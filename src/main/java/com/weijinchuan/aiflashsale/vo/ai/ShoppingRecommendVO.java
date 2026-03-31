package com.weijinchuan.aiflashsale.vo.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 导购返回对象
 */
@Data
public class ShoppingRecommendVO {

    /**
     * 原始查询
     */
    private String query;

    /**
     * 解析后的意图
     */
    private ShoppingIntentVO intent;

    /**
     * 推荐结果
     */
    private List<ShoppingRecommendItemVO> recommendations;
}