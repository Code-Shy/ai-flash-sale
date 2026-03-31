package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项返回对象
 */
@Data
public class CartItemVO {

    private Long itemId;
    private Long skuId;
    private String skuName;
    private String specs;
    private String imageUrl;
    private Integer quantity;
    private Integer checked;
    private BigDecimal priceSnapshot;
    private BigDecimal totalAmount;
}