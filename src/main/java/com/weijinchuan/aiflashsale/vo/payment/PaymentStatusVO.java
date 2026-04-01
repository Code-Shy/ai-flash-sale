package com.weijinchuan.aiflashsale.vo.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付状态视图
 */
@Data
public class PaymentStatusVO {

    private Long orderId;
    private String orderNo;
    private Integer orderStatus;
    private String paymentNo;
    private String paymentChannel;
    private Integer paymentStatus;
    private String outTradeNo;
    private String providerTradeNo;
    private String providerStatus;
    private BigDecimal amount;
    private LocalDateTime expireTime;
    private LocalDateTime successTime;
    private String message;
}
