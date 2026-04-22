package com.weijinchuan.aiflashsale.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * 手动 Ack 的 KafkaListenerContainerFactory
 *
 * OrderTimeoutConsumer 使用此 factory，以便：
 * 1. 手动控制 offset 提交（消息确认消费）
 * 2. nack 时不提交 offset，让 Kafka 重投（模拟延时重试）
 *
 * max.poll.interval.ms 控制 nack 后重投的最小间隔，
 * 设为 10s，避免未到期消息被过于频繁地重投。
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${order.timeout.nack.sleep-ms:10000}")
    private int nackSleepMs;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> manualAckKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // 手动提交 offset
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
