package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项返回对象
 */
@Data
public class OrderItemVO {

    private Long skuId;
    private String skuName;
    private String skuImage;
    private BigDecimal salePrice;
    private Integer quantity;
    private BigDecimal totalAmount;
}