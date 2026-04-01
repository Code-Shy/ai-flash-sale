package com.weijinchuan.aiflashsale.event.consumer;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.event.OrderCompletedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 订单完成消息消费者
 */
@Slf4j
@Component
public class OrderCompletedConsumer {

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_COMPLETED_TOPIC,
            groupId = "ai-flash-sale-order-group"
    )
    public void onMessage(OrderCompletedMessage message) {
        log.info("收到 Kafka 订单完成消息，message={}", message);
        log.info("模拟触发订单完成通知，orderNo={}, userId={}",
                message.getOrderNo(), message.getUserId());
        log.info("模拟更新复购和履约指标，orderId={}, completedTime={}",
                message.getOrderId(), message.getCompletedTime());
    }
}
