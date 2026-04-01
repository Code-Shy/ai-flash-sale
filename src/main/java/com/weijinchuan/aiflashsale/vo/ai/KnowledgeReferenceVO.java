package com.weijinchuan.aiflashsale.vo.ai;

import lombok.Data;

/**
 * 知识引用片段
 */
@Data
public class KnowledgeReferenceVO {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 内容摘要
     */
    private String contentPreview;

    /**
     * 检索分数
     */
    private Integer score;
}
