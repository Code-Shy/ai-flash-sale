package com.weijinchuan.aiflashsale.vo.ai;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 推荐商品项
 */
@Data
public class ShoppingRecommendItemVO {

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String skuName;

    /**
     * 商品规格
     */
    private String specs;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     * 销售价
     */
    private BigDecimal salePrice;

    /**
     * 推荐理由
     */
    private String reason;
}