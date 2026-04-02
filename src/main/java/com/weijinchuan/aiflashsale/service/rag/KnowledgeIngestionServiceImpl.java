package com.weijinchuan.aiflashsale.service.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.domain.KnowledgeChunk;
import com.weijinchuan.aiflashsale.domain.KnowledgeDoc;
import com.weijinchuan.aiflashsale.mapper.KnowledgeChunkMapper;
import com.weijinchuan.aiflashsale.mapper.KnowledgeDocMapper;
import com.weijinchuan.aiflashsale.vo.ai.KnowledgeSyncVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 知识库导入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestionServiceImpl implements KnowledgeIngestionService {

    private static final int STATUS_ACTIVE = 1;

    private final KnowledgeResourceLoader knowledgeResourceLoader;
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final ObjectMapper objectMapper;

    @Value("${rag.ingestion.source:classpath-json}")
    private String sourceType;

    @Value("${rag.ingestion.chunk-size:120}")
    private int chunkSize;

    @Value("${rag.ingestion.chunk-overlap:24}")
    private int chunkOverlap;

    @Value("${rag.bootstrap-from-classpath-on-startup:false}")
    private boolean bootstrapFromClasspathOnStartup;

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrapOnStartup() {
        if (!bootstrapFromClasspathOnStartup) {
            return;
        }

        try {
            long count = knowledgeDocMapper.selectCount(
                    new LambdaQueryWrapper<KnowledgeDoc>().eq(KnowledgeDoc::getStatus, STATUS_ACTIVE)
            );
            if (count > 0) {
                log.info("数据库知识库已存在有效文档，跳过 classpath 自动导入，count={}", count);
                return;
            }
            KnowledgeSyncVO result = syncFromClasspath();
            log.info("数据库知识库自动导入完成，result={}", result);
        } catch (Exception e) {
            log.warn("数据库知识库自动导入失败，将继续使用 classpath 兜底检索", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeSyncVO syncFromClasspath() {
        List<KnowledgeDocument> documents = knowledgeResourceLoader.loadFromClasspath();

        int syncedDocuments = 0;
        int generatedChunks = 0;
        for (KnowledgeDocument document : documents) {
            KnowledgeDoc persisted = upsertDocument(document);
            generatedChunks += rebuildChunks(persisted, document);
            syncedDocuments++;
        }

        KnowledgeSyncVO result = new KnowledgeSyncVO();
        result.setSource(sourceType + ":" + knowledgeResourceLoader.getKnowledgeBaseLocation());
        result.setTotalDocuments(documents.size());
        result.setSyncedDocuments(syncedDocuments);
        result.setGeneratedChunks(generatedChunks);
        result.setMessage("知识文档已同步到数据库并完成分片");
        return result;
    }

    private KnowledgeDoc upsertDocument(KnowledgeDocument document) {
        KnowledgeDoc existing = knowledgeDocMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeDoc>()
                        .eq(KnowledgeDoc::getDocCode, document.getId())
                        .last("LIMIT 1")
        );

        if (existing == null) {
            KnowledgeDoc knowledgeDoc = new KnowledgeDoc();
            knowledgeDoc.setDocCode(document.getId());
            knowledgeDoc.setTitle(document.getTitle());
            knowledgeDoc.setContent(document.getContent());
            knowledgeDoc.setDocType(document.getDocType());
            knowledgeDoc.setStoreId(document.getStoreId());
            knowledgeDoc.setSkuId(document.getSkuId());
            knowledgeDoc.setCategory(document.getCategory());
            knowledgeDoc.setTagsJson(serializeTags(document.getTags()));
            knowledgeDoc.setSourceType(sourceType);
            knowledgeDoc.setStatus(STATUS_ACTIVE);
            knowledgeDoc.setVersion(1);
            knowledgeDocMapper.insert(knowledgeDoc);
            return knowledgeDoc;
        }

        existing.setTitle(document.getTitle());
        existing.setContent(document.getContent());
        existing.setDocType(document.getDocType());
        existing.setStoreId(document.getStoreId());
        existing.setSkuId(document.getSkuId());
        existing.setCategory(document.getCategory());
        existing.setTagsJson(serializeTags(document.getTags()));
        existing.setSourceType(sourceType);
        existing.setStatus(STATUS_ACTIVE);
        existing.setVersion(existing.getVersion() == null ? 1 : existing.getVersion() + 1);
        knowledgeDocMapper.updateById(existing);
        return existing;
    }

    private int rebuildChunks(KnowledgeDoc persisted, KnowledgeDocument document) {
        knowledgeChunkMapper.delete(
                new LambdaQueryWrapper<KnowledgeChunk>()
                        .eq(KnowledgeChunk::getDocumentId, persisted.getId())
        );

        List<String> chunks = splitIntoChunks(document.getContent());
        int index = 0;
        for (String chunkContent : chunks) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(persisted.getId());
            chunk.setChunkIndex(index++);
            chunk.setStoreId(document.getStoreId());
            chunk.setSkuId(document.getSkuId());
            chunk.setCategory(document.getCategory());
            chunk.setTagsJson(serializeTags(document.getTags()));
            chunk.setContent(chunkContent);
            chunk.setContentPreview(buildPreview(chunkContent));
            chunk.setNormalizedText(normalize(chunkContent));
            chunk.setStatus(STATUS_ACTIVE);
            knowledgeChunkMapper.insert(chunk);
        }
        return chunks.size();
    }

    private List<String> splitIntoChunks(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        String normalized = content.trim();
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        int overlap = Math.max(0, Math.min(chunkOverlap, chunkSize / 2));
        int step = Math.max(1, chunkSize - overlap);

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + chunkSize);
            chunks.add(normalized.substring(start, end).trim());
            if (end >= normalized.length()) {
                break;
            }
            start += step;
        }
        return chunks;
    }

    private String buildPreview(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.length() <= 96) {
            return trimmed;
        }
        return trimmed.substring(0, 96) + "...";
    }

    private String serializeTags(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags == null ? List.of() : tags);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化知识标签失败", e);
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{Punct}\\s]+", "").toLowerCase(Locale.ROOT);
    }
}
