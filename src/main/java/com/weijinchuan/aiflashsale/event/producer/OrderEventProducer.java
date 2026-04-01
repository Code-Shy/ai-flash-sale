package com.weijinchuan.aiflashsale.event.producer;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
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
        kafkaTemplate.send(
                KafkaTopicConstants.ORDER_CREATED_TOPIC,
                String.valueOf(message.getOrderId()),
                message
        ).get(5, TimeUnit.SECONDS);

        log.info("Kafka 发送订单创建消息成功，topic={}, orderId={}, orderNo={}",
                KafkaTopicConstants.ORDER_CREATED_TOPIC,
                message.getOrderId(),
                message.getOrderNo());
    }
}
