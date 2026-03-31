package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表返回对象
 */
@Data
public class ProductListVO {

    private Long skuId;
    private String skuName;
    private String specs;
    private String imageUrl;
    private String categoryName;
    private BigDecimal salePrice;
}