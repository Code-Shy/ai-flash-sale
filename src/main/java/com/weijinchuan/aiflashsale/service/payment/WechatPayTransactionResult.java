package com.weijinchuan.aiflashsale.service.payment;

import java.time.LocalDateTime;

/**
 * 微信支付交易结果
 */
public record WechatPayTransactionResult(
        String outTradeNo,
        String providerTradeNo,
        String tradeState,
        String tradeStateDesc,
        Integer totalFen,
        LocalDateTime successTime,
        String rawPayload
) {
}
