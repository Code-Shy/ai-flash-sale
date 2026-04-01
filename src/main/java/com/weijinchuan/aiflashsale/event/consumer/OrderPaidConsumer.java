package com.weijinchuan.aiflashsale.event.consumer;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.event.OrderPaidMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 订单支付成功消息消费者
 */
@Slf4j
@Component
public class OrderPaidConsumer {

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAID_TOPIC,
            groupId = "ai-flash-sale-order-group"
    )
    public void onMessage(OrderPaidMessage message) {
        log.info("收到 Kafka 订单支付成功消息，message={}", message);
        log.info("模拟触发支付成功通知，orderNo={}, userId={}",
                message.getOrderNo(), message.getUserId());
        log.info("模拟更新支付统计指标，orderId={}, payAmount={}",
                message.getOrderId(), message.getPayAmount());
    }
}
