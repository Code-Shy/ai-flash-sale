package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品详情返回对象
 */
@Data
public class ProductDetailVO {

    private Long skuId;
    private String skuName;
    private String specs;
    private String unit;
    private String imageUrl;
    private String spuName;
    private String categoryName;
    private String brandName;
    private String description;
    private BigDecimal lowestPrice;
}