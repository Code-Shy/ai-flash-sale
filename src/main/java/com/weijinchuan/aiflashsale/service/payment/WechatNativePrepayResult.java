package com.weijinchuan.aiflashsale.service.payment;

/**
 * 微信 Native 预下单结果
 */
public record WechatNativePrepayResult(
        String codeUrl,
        String prepayPayload
) {
}
