package com.weijinchuan.aiflashsale.event;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付成功消息体
 */
@Data
public class OrderPaidMessage implements Serializable {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long storeId;
    private BigDecimal payAmount;
    private LocalDateTime paidTime;
}
