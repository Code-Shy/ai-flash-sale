package com.weijinchuan.aiflashsale.service.rag;

import lombok.Data;

/**
 * 检索结果
 */
@Data
public class RetrievedKnowledge {

    /**
     * 原始知识文档
     */
    private KnowledgeDocument document;

    /**
     * 检索分数
     */
    private Integer score;

    /**
     * 命中摘要
     */
    private String snippet;
}
