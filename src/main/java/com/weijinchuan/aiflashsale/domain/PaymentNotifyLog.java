package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付回调日志
 */
@Data
@TableName("payment_notify_log")
public class PaymentNotifyLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String paymentChannel;
    private String outTradeNo;
    private String providerTradeNo;
    private String notifyType;
    private String rawBody;
    private Integer processStatus;
    private String errorMessage;
    private LocalDateTime createTime;
}
