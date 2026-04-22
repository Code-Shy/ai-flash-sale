package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import com.weijinchuan.aiflashsale.event.OrderCompletedMessage;
import com.weijinchuan.aiflashsale.event.OrderPaidMessage;
import com.weijinchuan.aiflashsale.event.OrderTimeoutMessage;

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

    /**
     * 保存订单支付成功事件到 outbox。
     */
    Long saveOrderPaidEvent(OrderPaidMessage message);

    /**
     * 保存订单完成事件到 outbox。
     */
    Long saveOrderCompletedEvent(OrderCompletedMessage message);

    /**
     * 保存订单超时延时事件到 outbox。
     * 下单时调用，事务提交后通过 Outbox 保证消息不丢。
     */
    Long saveOrderTimeoutEvent(OrderTimeoutMessage message);
}
