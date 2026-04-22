package com.weijinchuan.aiflashsale.event.consumer;

import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.event.OrderTimeoutMessage;
import com.weijinchuan.aiflashsale.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时延时消息消费者
 *
 * 延时实现原理：
 *   Kafka 不支持原生延时消息，这里采用"Consumer 端时间检查 + nack 重投"方案：
 *   1. 消息投递时携带 executeAt（= 订单 expireTime）
 *   2. Consumer 收到消息后，若 now < executeAt，则 nack（不提交 offset），
 *      Kafka 会在 max.poll.interval.ms 后重新投递，直到到期
 *   3. 到期后执行 expireOrder，expireOrder 内部有 FOR UPDATE 行锁 + 状态校验，天然幂等
 *
 * 幂等去重：
 *   用 Redis SETNX 保证同一 orderId 只被成功处理一次。
 *   key TTL = 超时时长 + 1h，覆盖消息重投窗口。
 *
 * 崩溃恢复：
 *   见 OrderTimeoutRecoveryJob，启动时扫描 DB 补发漏掉的超时消息。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    // Redis key 前缀，用于幂等去重
    private static final String IDEMPOTENT_KEY_PREFIX = "order:timeout:processed:";

    private final OrderService orderService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${order.timeout.minutes:30}")
    private long orderTimeoutMinutes;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_TIMEOUT_TOPIC,
            groupId = "ai-flash-sale-order-timeout-group",
            // 手动提交 offset，确保消息确认消费
            containerFactory = "manualAckKafkaListenerContainerFactory"
    )
    public void onOrderTimeout(OrderTimeoutMessage message, Acknowledgment ack) {
        Long orderId = message.getOrderId();
        LocalDateTime executeAt = message.getExecuteAt();

        // 1. 检查是否到期，未到期则 nack，等待重投
        if (executeAt != null && LocalDateTime.now().isBefore(executeAt)) {
            long waitSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), executeAt);
            log.debug("订单超时消息未到期，nack 等待重投，orderId={}, 剩余{}秒", orderId, waitSeconds);
            // nack(sleepMillis=0)：立即放回，由 Kafka 重投（配合 max.poll.interval.ms 控制重试间隔）
            ack.nack(0);
            return;
        }

        // 2. Redis 幂等去重：SETNX，已处理则直接 ack 跳过
        String idempotentKey = IDEMPOTENT_KEY_PREFIX + orderId;
        // TTL = 超时时长 + 1h，足够覆盖所有重投窗口
        Boolean isFirstProcess = stringRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", orderTimeoutMinutes + 60, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isFirstProcess)) {
            log.info("订单超时消息已处理（幂等跳过），orderId={}", orderId);
            ack.acknowledge();
            return;
        }

        try {
            // 3. 执行关单（expireOrder 内部有 FOR UPDATE + 状态校验，天然幂等）
            orderService.expireOrder(orderId);
            log.info("订单超时关单成功，orderId={}", orderId);
            ack.acknowledge();
        } catch (Exception e) {
            // 关单失败：删除幂等 key，让消息重投重试
            stringRedisTemplate.delete(idempotentKey);
            log.warn("订单超时关单失败，nack 重试，orderId={}, reason={}", orderId, e.getMessage());
            ack.nack(0);
        }
    }
}
