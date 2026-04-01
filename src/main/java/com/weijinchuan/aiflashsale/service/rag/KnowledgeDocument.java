package com.weijinchuan.aiflashsale.service.rag;

import lombok.Data;

import java.util.List;

/**
 * 知识库文档
 */
@Data
public class KnowledgeDocument {

    /**
     * 文档 ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文
     */
    private String content;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 限定门店，null 表示通用
     */
    private Long storeId;

    /**
     * 限定商品，null 表示通用
     */
    private Long skuId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
}
