package com.weijinchuan.aiflashsale.service.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 知识资源加载器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeResourceLoader {

    private final ObjectMapper objectMapper;

    @Value("${rag.knowledge-base-location:ai/knowledge-base.json}")
    private String knowledgeBaseLocation;

    /**
     * 从 classpath 加载知识文档。
     */
    public List<KnowledgeDocument> loadFromClasspath() {
        ClassPathResource resource = new ClassPathResource(knowledgeBaseLocation);
        if (!resource.exists()) {
            log.warn("RAG 知识库资源不存在，location={}", knowledgeBaseLocation);
            return List.of();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            List<KnowledgeDocument> loaded = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<KnowledgeDocument>>() {}
            );
            return loaded == null ? List.of() : loaded;
        } catch (Exception e) {
            log.warn("RAG 知识库加载失败，location={}", knowledgeBaseLocation, e);
            return List.of();
        }
    }

    public String getKnowledgeBaseLocation() {
        return knowledgeBaseLocation;
    }
}
