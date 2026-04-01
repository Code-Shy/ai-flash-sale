package com.weijinchuan.aiflashsale.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.config.WechatPayProperties;
import com.weijinchuan.aiflashsale.domain.Orders;
import com.weijinchuan.aiflashsale.domain.PaymentOrder;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 微信 Native 支付 Provider
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true")
public class WechatNativePayProvider {

    private final NativePayService nativePayService;
    private final NotificationParser notificationParser;
    private final WechatPayProperties properties;
    private final ObjectMapper objectMapper;

    public WechatNativePrepayResult createNativePrepay(Orders order, PaymentOrder paymentOrder) {
        try {
            PrepayRequest request = new PrepayRequest();
            request.setAppid(properties.getAppId());
            request.setMchid(properties.getMerchantId());
            request.setDescription(buildDescription(order));
            request.setNotifyUrl(properties.getNotifyUrl());
            request.setOutTradeNo(paymentOrder.getOutTradeNo());
            request.setTimeExpire(formatTimeExpire(paymentOrder.getExpireTime()));

            Amount amount = new Amount();
            amount.setTotal(toFen(paymentOrder.getAmount()));
            request.setAmount(amount);

            PrepayResponse response = nativePayService.prepay(request);
            return new WechatNativePrepayResult(
                    response.getCodeUrl(),
                    objectMapper.writeValueAsString(response)
            );
        } catch (Exception e) {
            throw new BizException(5007, "微信支付预下单失败: " + e.getMessage());
        }
    }

    public WechatPayTransactionResult parseTransactionNotify(String body,
                                                             String serialNumber,
                                                             String nonce,
                                                             String signature,
                                                             String timestamp) {
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serialNumber)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timestamp)
                    .body(body)
                    .build();
            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            return mapTransaction(transaction);
        } catch (Exception e) {
            throw new BizException(5008, "微信支付回调验签失败: " + e.getMessage());
        }
    }

    public WechatPayTransactionResult queryByOutTradeNo(String outTradeNo) {
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(properties.getMerchantId());
            request.setOutTradeNo(outTradeNo);
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
            return mapTransaction(transaction);
        } catch (Exception e) {
            throw new BizException(5009, "微信支付查单失败: " + e.getMessage());
        }
    }

    public void closeOrder(String outTradeNo) {
        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(properties.getMerchantId());
            request.setOutTradeNo(outTradeNo);
            nativePayService.closeOrder(request);
        } catch (Exception e) {
            throw new BizException(5010, "微信支付关单失败: " + e.getMessage());
        }
    }

    private WechatPayTransactionResult mapTransaction(Transaction transaction) throws JsonProcessingException {
        Integer totalFen = transaction.getAmount() == null ? null : transaction.getAmount().getTotal();
        return new WechatPayTransactionResult(
                transaction.getOutTradeNo(),
                transaction.getTransactionId(),
                nullableString(transaction.getTradeState()),
                nullableString(transaction.getTradeStateDesc()),
                totalFen,
                parseProviderTime(transaction.getSuccessTime()),
                objectMapper.writeValueAsString(transaction)
        );
    }

    private String buildDescription(Orders order) {
        String prefix = properties.getDescriptionPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "闪购订单";
        }
        return prefix + "-" + order.getOrderNo();
    }

    private String formatTimeExpire(LocalDateTime expireTime) {
        if (expireTime == null) {
            return null;
        }
        return expireTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private int toFen(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    private LocalDateTime parseProviderTime(Object successTime) {
        if (successTime == null) {
            return null;
        }
        if (successTime instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }
        String value = String.valueOf(successTime);
        if (value.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(value).toLocalDateTime();
    }

    private String nullableString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
