package com.weijinchuan.aiflashsale.common.constant;

/**
 * Outbox 事件类型常量
 */
public final class OutboxEventTypeConstants {

    private OutboxEventTypeConstants() {
    }

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_PAID = "ORDER_PAID";
    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
}
