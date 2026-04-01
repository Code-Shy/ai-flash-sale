package com.weijinchuan.aiflashsale.common.constant;

/**
 * 订单状态常量
 */
public final class OrderStatusConstants {

    private OrderStatusConstants() {
    }

    /**
     * 待支付
     */
    public static final int PENDING_PAYMENT = 10;

    /**
     * 已支付
     */
    public static final int PAID = 20;

    /**
     * 已取消
     */
    public static final int CANCELED = 30;

    /**
     * 已完成
     */
    public static final int COMPLETED = 40;
}
