package com.weijinchuan.aiflashsale.common.constant;

/**
 * 支付回调处理状态
 */
public final class PaymentNotifyProcessStatusConstants {

    private PaymentNotifyProcessStatusConstants() {
    }

    public static final int RECEIVED = 10;
    public static final int SUCCESS = 20;
    public static final int IGNORED = 30;
    public static final int FAILED = 40;
    public static final int ABNORMAL = 50;
}
