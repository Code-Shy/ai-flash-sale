package com.weijinchuan.aiflashsale.service.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单知识检索器
 *
 * 当前实现使用 classpath 中的知识库 JSON 做轻量检索，
 * 后续可以替换为向量库或混合检索而不影响业务层。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeRetriever {

    private static final Pattern TERM_PATTERN = Pattern.compile("[\\p{IsHan}A-Za-z0-9]{2,}");

    private final ObjectMapper objectMapper;

    @Value("${rag.knowledge-base-location:ai/knowledge-base.json}")
    private String knowledgeBaseLocation;

    @Value("${rag.retrieval.default-top-k:4}")
    private int defaultTopK;

    @Value("${rag.retrieval.preview-length:88}")
    private int previewLength;

    private List<KnowledgeDocument> documents = List.of();

    @PostConstruct
    public void loadKnowledgeBase() {
        ClassPathResource resource = new ClassPathResource(knowledgeBaseLocation);
        if (!resource.exists()) {
            log.warn("RAG 知识库资源不存在，location={}", knowledgeBaseLocation);
            documents = List.of();
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            List<KnowledgeDocument> loaded = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<KnowledgeDocument>>() {}
            );
            documents = loaded == null ? List.of() : loaded;
            log.info("RAG 知识库加载完成，location={}, size={}", knowledgeBaseLocation, documents.size());
        } catch (Exception e) {
            log.warn("RAG 知识库加载失败，location={}", knowledgeBaseLocation, e);
            documents = List.of();
        }
    }

    /**
     * 检索知识片段
     */
    public List<RetrievedKnowledge> search(Long storeId, Long skuId, String query, int limit) {
        if (isBlank(query) || documents.isEmpty()) {
            return List.of();
        }

        int topK = limit > 0 ? limit : defaultTopK;
        String normalizedQuery = normalize(query);
        List<String> terms = extractTerms(query);

        List<RetrievedKnowledge> results = new ArrayList<>();
        for (KnowledgeDocument document : documents) {
            if (!withinScope(document, storeId, skuId)) {
                continue;
            }

            int score = scoreDocument(document, storeId, skuId, normalizedQuery, terms);
            if (score <= 0) {
                continue;
            }

            RetrievedKnowledge retrieved = new RetrievedKnowledge();
            retrieved.setDocument(document);
            retrieved.setScore(score);
            retrieved.setSnippet(buildSnippet(document.getContent(), normalizedQuery, terms));
            results.add(retrieved);
        }

        results.sort(Comparator.comparing(RetrievedKnowledge::getScore).reversed());
        if (results.size() <= topK) {
            return results;
        }
        return new ArrayList<>(results.subList(0, topK));
    }

    private boolean withinScope(KnowledgeDocument document, Long storeId, Long skuId) {
        if (storeId != null && document.getStoreId() != null && !Objects.equals(storeId, document.getStoreId())) {
            return false;
        }
        return skuId == null || document.getSkuId() == null || Objects.equals(skuId, document.getSkuId());
    }

    private int scoreDocument(KnowledgeDocument document,
                              Long storeId,
                              Long skuId,
                              String normalizedQuery,
                              List<String> terms) {
        int score = 0;

        if (storeId != null && Objects.equals(storeId, document.getStoreId())) {
            score += 6;
        }
        if (skuId != null && Objects.equals(skuId, document.getSkuId())) {
            score += 10;
        }

        String normalizedText = normalize(
                safeText(document.getTitle()) + " "
                        + safeText(document.getContent()) + " "
                        + safeText(document.getCategory()) + " "
                        + String.join(" ", safeList(document.getTags()))
        );

        if (!normalizedQuery.isEmpty() && normalizedText.contains(normalizedQuery)) {
            score += 12;
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

        return score;
    }

    private String buildSnippet(String content, String normalizedQuery, List<String> terms) {
        if (isBlank(content)) {
            return "";
        }

        String trimmed = content.trim();
        String normalizedContent = normalize(trimmed);

        int hitIndex = -1;
        if (!normalizedQuery.isEmpty()) {
            hitIndex = normalizedContent.indexOf(normalizedQuery);
        }

        if (hitIndex < 0) {
            for (String term : terms) {
                hitIndex = normalizedContent.indexOf(term);
                if (hitIndex >= 0) {
                    break;
                }
            }
        }

        if (hitIndex < 0 || trimmed.length() <= previewLength) {
            return abbreviate(trimmed, previewLength);
        }

        int start = Math.max(0, hitIndex - (previewLength / 3));
        int end = Math.min(trimmed.length(), start + previewLength);
        return abbreviate(trimmed.substring(start, end), previewLength);
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
