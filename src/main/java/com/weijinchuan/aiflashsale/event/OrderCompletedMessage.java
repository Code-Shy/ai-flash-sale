package com.weijinchuan.aiflashsale.event;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单完成消息体
 */
@Data
public class OrderCompletedMessage implements Serializable {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long storeId;
    private BigDecimal payAmount;
    private LocalDateTime completedTime;
}
