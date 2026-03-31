package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车返回对象
 */
@Data
public class CartVO {

    private Long cartId;
    private Long userId;
    private Long storeId;
    private List<CartItemVO> items;
    private BigDecimal totalAmount;
}