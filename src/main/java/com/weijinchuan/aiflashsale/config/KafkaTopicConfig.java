package com.weijinchuan.aiflashsale.config;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka Topic 配置
 *
 * 说明：
 * 启动项目时自动创建 Topic，方便本地开发。
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * 创建订单创建 Topic
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic(KafkaTopicConstants.ORDER_CREATED_TOPIC, 1, (short) 1);
    }

    /**
     * 创建订单支付成功 Topic
     */
    @Bean
    public NewTopic orderPaidTopic() {
        return new NewTopic(KafkaTopicConstants.ORDER_PAID_TOPIC, 1, (short) 1);
    }

    /**
     * 创建订单完成 Topic
     */
    @Bean
    public NewTopic orderCompletedTopic() {
        return new NewTopic(KafkaTopicConstants.ORDER_COMPLETED_TOPIC, 1, (short) 1);
    }

    /**
     * 订单超时延时 Topic（主 + 重试，各 1 分区）
     */
    @Bean
    public NewTopic orderTimeoutTopic() {
        return new NewTopic(KafkaTopicConstants.ORDER_TIMEOUT_TOPIC, 1, (short) 1);
    }

    @Bean
    public NewTopic orderTimeoutRetryTopic() {
        return new NewTopic(KafkaTopicConstants.ORDER_TIMEOUT_RETRY_TOPIC, 1, (short) 1);
    }
}
