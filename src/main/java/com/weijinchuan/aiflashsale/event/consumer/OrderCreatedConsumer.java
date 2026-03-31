package com.weijinchuan.aiflashsale.event.consumer;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 订单创建消息消费者
 *
 * 当前先做最小消费逻辑：
 * 1. 打日志
 * 2. 模拟发送通知
 * 3. 模拟埋点
 */
@Slf4j
@Component
public class OrderCreatedConsumer {

    /**
     * 监听订单创建 Topic
     *
     * @param message 订单创建消息
     */
    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_CREATED_TOPIC,
            groupId = "ai-flash-sale-order-group"
    )
    public void onMessage(OrderCreatedMessage message) {
        log.info("收到 Kafka 订单创建消息，message={}", message);

        // 模拟通知逻辑
        log.info("模拟发送订单创建通知，orderNo={}, userId={}",
                message.getOrderNo(), message.getUserId());

        // 模拟埋点逻辑
        log.info("模拟写入订单创建埋点，orderId={}, payAmount={}",
                message.getOrderId(), message.getPayAmount());
    }
}