package com.weijinchuan.aiflashsale.vo.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 问答返回对象
 */
@Data
public class ShoppingAnswerVO {

    /**
     * 原始问题
     */
    private String query;

    /**
     * 解析后的意图
     */
    private ShoppingIntentVO intent;

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 相关商品推荐
     */
    private List<ShoppingRecommendItemVO> recommendations;

    /**
     * 参考知识
     */
    private List<KnowledgeReferenceVO> references;
}
