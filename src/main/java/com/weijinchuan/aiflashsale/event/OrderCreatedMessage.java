package com.weijinchuan.aiflashsale.event;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单创建消息体
 *
 * 用于下单成功后发送到 Kafka，
 * 后续可供通知、埋点、统计等异步逻辑消费。
 */
@Data
public class OrderCreatedMessage implements Serializable {

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 门店 ID
     */
    private Long storeId;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}