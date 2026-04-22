package com.weijinchuan.aiflashsale.event.producer;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import com.weijinchuan.aiflashsale.event.OrderCompletedMessage;
import com.weijinchuan.aiflashsale.event.OrderPaidMessage;
import com.weijinchuan.aiflashsale.event.OrderTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 订单事件生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送订单创建消息
     *
     * @param message 订单创建消息
     */
    public void sendOrderCreatedMessage(OrderCreatedMessage message) throws Exception {
        sendAndLog(KafkaTopicConstants.ORDER_CREATED_TOPIC, message.getOrderId(), message, message.getOrderNo(), "订单创建");
    }

    /**
     * 发送订单支付成功消息
     */
    public void sendOrderPaidMessage(OrderPaidMessage message) throws Exception {
        sendAndLog(KafkaTopicConstants.ORDER_PAID_TOPIC, message.getOrderId(), message, message.getOrderNo(), "订单支付成功");
    }

    /**
     * 发送订单完成消息
     */
    public void sendOrderCompletedMessage(OrderCompletedMessage message) throws Exception {
        sendAndLog(KafkaTopicConstants.ORDER_COMPLETED_TOPIC, message.getOrderId(), message, message.getOrderNo(), "订单完成");
    }

    /**
     * 发送订单超时延时消息
     * 消息投递到 order-timeout-topic，Consumer 端根据 executeAt 判断是否到期。
     */
    public void sendOrderTimeoutMessage(OrderTimeoutMessage message) throws Exception {
        sendAndLog(KafkaTopicConstants.ORDER_TIMEOUT_TOPIC, message.getOrderId(), message, String.valueOf(message.getOrderId()), "订单超时延时");
    }

    private void sendAndLog(String topic, Long orderId, Object message, String orderNo, String eventName) throws Exception {
        kafkaTemplate.send(topic, String.valueOf(orderId), message).get(5, TimeUnit.SECONDS);
        log.info("Kafka 发送{}消息成功，topic={}, orderId={}, orderNo={}",
                eventName, topic, orderId, orderNo);
    }
}
