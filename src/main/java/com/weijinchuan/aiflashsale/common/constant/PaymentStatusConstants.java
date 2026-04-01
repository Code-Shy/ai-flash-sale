package com.weijinchuan.aiflashsale.common.constant;

/**
 * 支付单状态常量
 */
public final class PaymentStatusConstants {

    private PaymentStatusConstants() {
    }

    /**
     * 支付中
     */
    public static final int PAYING = 10;

    /**
     * 支付成功
     */
    public static final int SUCCESS = 20;

    /**
     * 已关闭
     */
    public static final int CLOSED = 30;

    /**
     * 支付失败
     */
    public static final int FAILED = 40;

    /**
     * 异常单
     */
    public static final int ABNORMAL = 50;
}
