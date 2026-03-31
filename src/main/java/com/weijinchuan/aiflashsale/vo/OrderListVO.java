package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表返回对象
 */
@Data
public class OrderListVO {

    private Long orderId;
    private String orderNo;
    private Long storeId;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private LocalDateTime createTime;
}