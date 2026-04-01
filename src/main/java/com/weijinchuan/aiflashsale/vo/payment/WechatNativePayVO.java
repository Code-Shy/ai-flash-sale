package com.weijinchuan.aiflashsale.vo.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微信 Native 预下单返回
 */
@Data
public class WechatNativePayVO {

    private Long orderId;
    private String orderNo;
    private String paymentNo;
    private String outTradeNo;
    private String codeUrl;
    private BigDecimal amount;
    private LocalDateTime expireTime;
}
