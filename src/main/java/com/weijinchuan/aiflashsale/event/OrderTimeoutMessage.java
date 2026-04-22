package com.weijinchuan.aiflashsale.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单超时延时消息体
 *
 * executeAt：期望执行关单的时间点（= 订单 expireTime）。
 * Consumer 收到消息后若 now < executeAt，则 nack 触发重试，直到到期再执行。
 */
@Data
public class OrderTimeoutMessage implements Serializable {

    private Long orderId;

    /** 期望执行关单的时间（= 订单 expireTime） */
    private LocalDateTime executeAt;
}
