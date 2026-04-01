package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单实体
 */
@Data
@TableName("payment_order")
public class PaymentOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String paymentChannel;
    private BigDecimal amount;
    private Integer status;
    private String outTradeNo;
    private String providerTradeNo;
    private String codeUrl;
    private String prepayPayload;
    private String notifyPayload;
    private String providerStatus;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime expireTime;
    private LocalDateTime successTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
