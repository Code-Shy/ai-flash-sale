package com.weijinchuan.aiflashsale.service.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.domain.KnowledgeChunk;
import com.weijinchuan.aiflashsale.domain.KnowledgeDoc;
import com.weijinchuan.aiflashsale.mapper.KnowledgeChunkMapper;
import com.weijinchuan.aiflashsale.mapper.KnowledgeDocMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识检索器
 *
 * 优先从数据库知识文档 / 分片检索；
 * 如果数据库尚未初始化，则回退到 classpath JSON 知识库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeRetriever {

    private static final int STATUS_ACTIVE = 1;
    private static final Pattern TERM_PATTERN = Pattern.compile("[\\p{IsHan}A-Za-z0-9]{2,}");

    private final ObjectMapper objectMapper;
    private final KnowledgeResourceLoader knowledgeResourceLoader;
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;

    @Value("${rag.retrieval.default-top-k:4}")
    private int defaultTopK;

    @Value("${rag.retrieval.preview-length:88}")
    private int previewLength;

    @Value("${rag.retrieval.max-candidate-docs:300}")
    private int maxCandidateDocs;

    private List<KnowledgeDocument> fallbackDocuments = List.of();

    @PostConstruct
    public void loadFallbackKnowledgeBase() {
        fallbackDocuments = knowledgeResourceLoader.loadFromClasspath();
        log.info("RAG fallback 知识库加载完成，size={}", fallbackDocuments.size());
    }

    /**
     * 检索知识片段
     */
    public List<RetrievedKnowledge> search(Long storeId, Long skuId, String query, int limit) {
        if (isBlank(query)) {
            return List.of();
        }

        int topK = limit > 0 ? limit : defaultTopK;
        String normalizedQuery = normalize(query);
        List<String> terms = extractTerms(query);

        List<RetrievedKnowledge> databaseResults = searchFromDatabase(storeId, skuId, normalizedQuery, terms, topK);
        if (!databaseResults.isEmpty()) {
            return databaseResults;
        }

        return searchFromFallback(storeId, skuId, normalizedQuery, terms, topK);
    }

    private List<RetrievedKnowledge> searchFromDatabase(Long storeId,
                                                        Long skuId,
                                                        String normalizedQuery,
                                                        List<String> terms,
                                                        int topK) {
        try {
            List<KnowledgeDoc> docEntities = knowledgeDocMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeDoc>()
                            .eq(KnowledgeDoc::getStatus, STATUS_ACTIVE)
                            .orderByDesc(KnowledgeDoc::getUpdateTime)
                            .last("LIMIT " + maxCandidateDocs)
            );

            Map<Long, KnowledgeDocument> documents = new HashMap<>();
            for (KnowledgeDoc docEntity : docEntities) {
                KnowledgeDocument document = toKnowledgeDocument(docEntity);
                if (!withinScope(document, storeId, skuId)) {
                    continue;
                }
                documents.put(docEntity.getId(), document);
            }

            if (documents.isEmpty()) {
                return List.of();
            }

            List<KnowledgeChunk> chunks = knowledgeChunkMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeChunk>()
                            .eq(KnowledgeChunk::getStatus, STATUS_ACTIVE)
                            .in(KnowledgeChunk::getDocumentId, documents.keySet())
                            .orderByAsc(KnowledgeChunk::getDocumentId, KnowledgeChunk::getChunkIndex)
            );

            Map<Long, RetrievedKnowledge> bestByDocument = new HashMap<>();
            for (KnowledgeChunk chunk : chunks) {
                KnowledgeDocument document = documents.get(chunk.getDocumentId());
                if (document == null) {
                    continue;
                }

                int score = scoreChunk(document, chunk, normalizedQuery, terms);
                if (score <= 0) {
                    continue;
                }

                RetrievedKnowledge currentBest = bestByDocument.get(chunk.getDocumentId());
                if (currentBest != null && currentBest.getScore() >= score) {
                    continue;
                }

                RetrievedKnowledge retrieved = new RetrievedKnowledge();
                retrieved.setDocument(document);
                retrieved.setScore(score);
                retrieved.setSnippet(buildSnippet(chunk.getContent(), normalizedQuery, terms));
                bestByDocument.put(chunk.getDocumentId(), retrieved);
            }

            return sortAndTrim(new ArrayList<>(bestByDocument.values()), topK);
        } catch (Exception e) {
            log.warn("数据库知识检索失败，将回退到 classpath JSON", e);
            return List.of();
        }
    }

    private List<RetrievedKnowledge> searchFromFallback(Long storeId,
                                                        Long skuId,
                                                        String normalizedQuery,
                                                        List<String> terms,
                                                        int topK) {
        if (fallbackDocuments.isEmpty()) {
            return List.of();
        }

        List<RetrievedKnowledge> results = new ArrayList<>();
        for (KnowledgeDocument document : fallbackDocuments) {
            if (!withinScope(document, storeId, skuId)) {
                continue;
            }

            int score = scoreDocument(document, normalizedQuery, terms);
            if (score <= 0) {
                continue;
            }

            RetrievedKnowledge retrieved = new RetrievedKnowledge();
            retrieved.setDocument(document);
            retrieved.setScore(score);
            retrieved.setSnippet(buildSnippet(document.getContent(), normalizedQuery, terms));
            results.add(retrieved);
        }
        return sortAndTrim(results, topK);
    }

    private List<RetrievedKnowledge> sortAndTrim(List<RetrievedKnowledge> results, int topK) {
        results.sort(Comparator.comparing(RetrievedKnowledge::getScore).reversed());
        if (results.size() <= topK) {
            return results;
        }
        return new ArrayList<>(results.subList(0, topK));
    }

    private int scoreChunk(KnowledgeDocument document,
                           KnowledgeChunk chunk,
                           String normalizedQuery,
                           List<String> terms) {
        int score = scoreDocument(document, normalizedQuery, terms);
        String normalizedChunk = safeText(chunk.getNormalizedText());

        if (!normalizedQuery.isEmpty() && normalizedChunk.contains(normalizedQuery)) {
            score += 15;
        }
        for (String term : terms) {
            if (term.length() >= 2 && normalizedChunk.contains(term)) {
                score += Math.min(6, term.length() + 2);
            }
        }
        return score;
    }

    private int scoreDocument(KnowledgeDocument document,
                              String normalizedQuery,
                              List<String> terms) {
        int score = 0;

        String normalizedText = normalize(
                safeText(document.getTitle()) + " "
                        + safeText(document.getContent()) + " "
                        + safeText(document.getCategory()) + " "
                        + String.join(" ", safeList(document.getTags()))
        );

        if (!normalizedQuery.isEmpty() && normalizedText.contains(normalizedQuery)) {
            score += 10;
        }

        String normalizedCategory = normalize(document.getCategory());
        if (!normalizedCategory.isEmpty() && normalizedQuery.contains(normalizedCategory)) {
            score += 5;
        }

        for (String tag : safeList(document.getTags())) {
            String normalizedTag = normalize(tag);
            if (!normalizedTag.isEmpty()
                    && (normalizedQuery.contains(normalizedTag) || normalizedTag.contains(normalizedQuery))) {
                score += 4;
            }
        }

        for (String term : terms) {
            if (term.length() < 2) {
                continue;
            }
            if (normalizedText.contains(term)) {
                score += Math.min(5, term.length() + 1);
            }
        }

        if (document.getStoreId() != null) {
            score += 3;
        }
        if (document.getSkuId() != null) {
            score += 4;
        }
        return score;
    }

    private KnowledgeDocument toKnowledgeDocument(KnowledgeDoc entity) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(entity.getDocCode());
        document.setTitle(entity.getTitle());
        document.setContent(entity.getContent());
        document.setDocType(entity.getDocType());
        document.setStoreId(entity.getStoreId());
        document.setSkuId(entity.getSkuId());
        document.setCategory(entity.getCategory());
        document.setTags(parseTags(entity.getTagsJson()));
        return document;
    }

    private List<String> parseTags(String tagsJson) {
        if (isBlank(tagsJson)) {
            return List.of();
        }
        try {
            List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
            return tags == null ? List.of() : tags;
        } catch (Exception e) {
            log.warn("解析知识标签失败，tagsJson={}", tagsJson, e);
            return List.of();
        }
    }

    private boolean withinScope(KnowledgeDocument document, Long storeId, Long skuId) {
        if (storeId != null && document.getStoreId() != null && !Objects.equals(storeId, document.getStoreId())) {
            return false;
        }
        return skuId == null || document.getSkuId() == null || Objects.equals(skuId, document.getSkuId());
    }

    private String buildSnippet(String content, String normalizedQuery, List<String> terms) {
        if (isBlank(content)) {
            return "";
        }

        String trimmed = content.trim();
        int hitIndex = -1;
        String lowerText = trimmed.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            hitIndex = lowerText.indexOf(term.toLowerCase(Locale.ROOT));
            if (hitIndex >= 0) {
                break;
            }
        }

        if (hitIndex < 0 || trimmed.length() <= previewLength) {
            return abbreviate(trimmed, previewLength);
        }

        int start = Math.max(0, Math.min(trimmed.length() - 1, hitIndex));
        int snippetStart = Math.max(0, start - (previewLength / 3));
        int end = Math.min(trimmed.length(), snippetStart + previewLength);
        return abbreviate(trimmed.substring(snippetStart, end), previewLength);
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "...";
    }

    private List<String> extractTerms(String query) {
        Set<String> terms = new LinkedHashSet<>();
        Matcher matcher = TERM_PATTERN.matcher(query);
        while (matcher.find() && terms.size() < 20) {
            String segment = normalize(matcher.group());
            if (segment.length() < 2) {
                continue;
            }
            terms.add(segment);

            if (containsChinese(segment) && segment.length() <= 8) {
                addNgrams(segment, terms);
            }
        }
        return new ArrayList<>(terms);
    }

    private void addNgrams(String segment, Set<String> terms) {
        for (int gram = 2; gram <= Math.min(4, segment.length()); gram++) {
            for (int i = 0; i <= segment.length() - gram && terms.size() < 20; i++) {
                terms.add(segment.substring(i, i + gram));
            }
        }
    }

    private boolean containsChinese(String text) {
        for (char ch : text.toCharArray()) {
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{Punct}\\s]+", "").toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
