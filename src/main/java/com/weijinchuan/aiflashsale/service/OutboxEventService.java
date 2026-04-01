package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;

/**
 * 外盒事件服务
 */
public interface OutboxEventService {

    /**
     * 保存订单创建事件到 outbox。
     *
     * @param message 订单创建消息
     * @return outbox 事件 ID
     */
    Long saveOrderCreatedEvent(OrderCreatedMessage message);
}
