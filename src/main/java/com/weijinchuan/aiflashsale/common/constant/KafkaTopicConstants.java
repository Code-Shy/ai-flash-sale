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
}
