package com.weijinchuan.aiflashsale.common.constant;

/**
 * Kafka Topic 常量
 */
public final class KafkaTopicConstants {

    private KafkaTopicConstants() {
    }

    /**
     * 订单创建 Topic
     */
    public static final String ORDER_CREATED_TOPIC = "order-created-topic";

    /**
     * 订单支付成功 Topic
     */
    public static final String ORDER_PAID_TOPIC = "order-paid-topic";

    /**
     * 订单完成 Topic
     */
    public static final String ORDER_COMPLETED_TOPIC = "order-completed-topic";

    /**
     * 订单超时延时 Topic
     * Consumer 收到消息后检查 executeAt，未到期则 nack 触发重试，到期则执行关单。
     */
    public static final String ORDER_TIMEOUT_TOPIC = "order-timeout-topic";

    /** 超时消息重试 Topic（Spring Retry Topic 自动路由） */
    public static final String ORDER_TIMEOUT_RETRY_TOPIC = "order-timeout-topic-retry";
}
