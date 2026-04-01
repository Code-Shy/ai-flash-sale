package com.weijinchuan.aiflashsale.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weijinchuan.aiflashsale.common.constant.OrderStatusConstants;
import com.weijinchuan.aiflashsale.common.constant.PaymentChannelConstants;
import com.weijinchuan.aiflashsale.common.constant.PaymentNotifyProcessStatusConstants;
import com.weijinchuan.aiflashsale.common.constant.PaymentStatusConstants;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.Orders;
import com.weijinchuan.aiflashsale.domain.PaymentNotifyLog;
import com.weijinchuan.aiflashsale.domain.PaymentOrder;
import com.weijinchuan.aiflashsale.dto.payment.WechatNativePayDTO;
import com.weijinchuan.aiflashsale.mapper.OrdersMapper;
import com.weijinchuan.aiflashsale.mapper.PaymentNotifyLogMapper;
import com.weijinchuan.aiflashsale.mapper.PaymentOrderMapper;
import com.weijinchuan.aiflashsale.service.OrderService;
import com.weijinchuan.aiflashsale.service.PaymentService;
import com.weijinchuan.aiflashsale.service.payment.WechatNativePayProvider;
import com.weijinchuan.aiflashsale.service.payment.WechatNativePrepayResult;
import com.weijinchuan.aiflashsale.service.payment.WechatPayTransactionResult;
import com.weijinchuan.aiflashsale.vo.payment.PaymentStatusVO;
import com.weijinchuan.aiflashsale.vo.payment.WechatNativePayVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 支付服务实现
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String NOTIFY_TYPE_TRANSACTION = "TRANSACTION";

    private final OrdersMapper ordersMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentNotifyLogMapper paymentNotifyLogMapper;
    private final OrderService orderService;
    private final ObjectProvider<WechatNativePayProvider> wechatNativePayProviderObjectProvider;

    @Override
    public WechatNativePayVO createWechatNativePay(WechatNativePayDTO dto) {
        Orders order = getAndValidatePayableOrder(dto.getUserId(), dto.getOrderId());

        PaymentOrder reusablePaymentOrder = getReusablePaymentOrder(order.getId());
        if (reusablePaymentOrder != null
                && StringUtils.hasText(reusablePaymentOrder.getCodeUrl())
                && !isExpired(reusablePaymentOrder.getExpireTime())) {
            return buildWechatNativePayVO(order, reusablePaymentOrder);
        }

        PaymentOrder paymentOrder = createLocalPaymentOrder(order);

        try {
            WechatNativePrepayResult prepayResult = getWechatNativePayProvider().createNativePrepay(order, paymentOrder);
            paymentOrder.setCodeUrl(prepayResult.codeUrl());
            paymentOrder.setPrepayPayload(prepayResult.prepayPayload());
            paymentOrder.setProviderStatus("PREPAY_CREATED");
            paymentOrder.setErrorCode(null);
            paymentOrder.setErrorMessage(null);
            paymentOrderMapper.updateById(paymentOrder);
            return buildWechatNativePayVO(order, paymentOrder);
        } catch (BizException e) {
            markPaymentFailed(paymentOrder, "WECHAT_PREPAY_FAILED", e.getMessage());
            throw e;
        }
    }

    @Override
    public PaymentStatusVO getPaymentStatus(Long userId, Long orderId) {
        Orders order = getOrderAndValidateOwner(userId, orderId);
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderId, orderId)
                        .orderByDesc(PaymentOrder::getId)
                        .last("LIMIT 1")
        );

        PaymentStatusVO vo = new PaymentStatusVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());

        if (paymentOrder != null) {
            vo.setPaymentNo(paymentOrder.getPaymentNo());
            vo.setPaymentChannel(paymentOrder.getPaymentChannel());
            vo.setPaymentStatus(paymentOrder.getStatus());
            vo.setOutTradeNo(paymentOrder.getOutTradeNo());
            vo.setProviderTradeNo(paymentOrder.getProviderTradeNo());
            vo.setProviderStatus(paymentOrder.getProviderStatus());
            vo.setAmount(paymentOrder.getAmount());
            vo.setExpireTime(paymentOrder.getExpireTime());
            vo.setSuccessTime(paymentOrder.getSuccessTime());
        } else {
            vo.setAmount(order.getPayAmount());
            vo.setExpireTime(order.getExpireTime());
        }

        vo.setMessage(buildPaymentStatusMessage(order, paymentOrder));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatNativePayNotify(String body,
                                            String serialNumber,
                                            String nonce,
                                            String signature,
                                            String timestamp) {
        PaymentNotifyLog notifyLog = new PaymentNotifyLog();
        notifyLog.setPaymentChannel(PaymentChannelConstants.WECHAT_NATIVE);
        notifyLog.setNotifyType(NOTIFY_TYPE_TRANSACTION);
        notifyLog.setRawBody(body);
        notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.RECEIVED);

        try {
            WechatPayTransactionResult transaction = getWechatNativePayProvider().parseTransactionNotify(
                    body, serialNumber, nonce, signature, timestamp
            );
            handleNotifyTransaction(transaction, notifyLog);
        } catch (Exception e) {
            notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.FAILED);
            notifyLog.setErrorMessage(trimErrorMessage(e.getMessage()));
            paymentNotifyLogMapper.insert(notifyLog);
            throw e;
        }
    }

    private void handleNotifyTransaction(WechatPayTransactionResult transaction, PaymentNotifyLog notifyLog) {
        notifyLog.setOutTradeNo(transaction.outTradeNo());
        notifyLog.setProviderTradeNo(transaction.providerTradeNo());

        PaymentOrder paymentOrder = paymentOrderMapper.selectByOutTradeNoForUpdate(transaction.outTradeNo());
        if (paymentOrder == null) {
            throw new BizException(404, "支付单不存在");
        }

        validateNotifyAmount(paymentOrder, transaction.totalFen());

        if (paymentOrder.getStatus() == PaymentStatusConstants.SUCCESS) {
            notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.IGNORED);
            notifyLog.setErrorMessage("支付单已处理成功，忽略重复回调");
            paymentNotifyLogMapper.insert(notifyLog);
            return;
        }

        paymentOrder.setProviderTradeNo(transaction.providerTradeNo());
        paymentOrder.setNotifyPayload(transaction.rawPayload());
        paymentOrder.setProviderStatus(transaction.tradeState());

        String tradeState = transaction.tradeState();
        if ("SUCCESS".equals(tradeState)) {
            handleSuccessfulPayment(paymentOrder, transaction, notifyLog);
        } else if ("CLOSED".equals(tradeState) || "REVOKED".equals(tradeState)) {
            paymentOrder.setStatus(PaymentStatusConstants.CLOSED);
            paymentOrder.setCloseTime(LocalDateTime.now());
            paymentOrder.setErrorMessage(trimErrorMessage(transaction.tradeStateDesc()));
            paymentOrderMapper.updateById(paymentOrder);

            notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.SUCCESS);
            paymentNotifyLogMapper.insert(notifyLog);
        } else if ("PAYERROR".equals(tradeState)) {
            markPaymentFailed(paymentOrder, tradeState, transaction.tradeStateDesc());
            notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.SUCCESS);
            paymentNotifyLogMapper.insert(notifyLog);
        } else {
            paymentOrderMapper.updateById(paymentOrder);
            notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.IGNORED);
            notifyLog.setErrorMessage("当前交易状态无需处理: " + tradeState);
            paymentNotifyLogMapper.insert(notifyLog);
        }
    }

    private void handleSuccessfulPayment(PaymentOrder paymentOrder,
                                         WechatPayTransactionResult transaction,
                                         PaymentNotifyLog notifyLog) {
        try {
            orderService.confirmPaidOrder(paymentOrder.getUserId(), paymentOrder.getOrderId());
            markPaymentSuccess(paymentOrder, transaction);
            notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.SUCCESS);
            paymentNotifyLogMapper.insert(notifyLog);
        } catch (BizException e) {
            if (e.getCode() != null && e.getCode() == 4005) {
                markPaymentAbnormal(paymentOrder, transaction, "订单已关闭，但收到支付成功回调");
                notifyLog.setProcessStatus(PaymentNotifyProcessStatusConstants.ABNORMAL);
                notifyLog.setErrorMessage("订单已关闭，但支付平台返回成功");
                paymentNotifyLogMapper.insert(notifyLog);
                return;
            }
            throw e;
        }
    }

    private Orders getAndValidatePayableOrder(Long userId, Long orderId) {
        Orders order = getOrderAndValidateOwner(userId, orderId);
        if (order.getOrderStatus() != OrderStatusConstants.PENDING_PAYMENT) {
            throw new BizException(4010, "当前订单状态不允许发起支付");
        }
        if (isExpired(order.getExpireTime())) {
            throw new BizException(4011, "订单已超时，请重新下单");
        }
        return order;
    }

    private Orders getOrderAndValidateOwner(Long userId, Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BizException(403, "无权操作该订单");
        }
        return order;
    }

    private PaymentOrder getReusablePaymentOrder(Long orderId) {
        return paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderId, orderId)
                        .eq(PaymentOrder::getPaymentChannel, PaymentChannelConstants.WECHAT_NATIVE)
                        .eq(PaymentOrder::getStatus, PaymentStatusConstants.PAYING)
                        .orderByDesc(PaymentOrder::getId)
                        .last("LIMIT 1")
        );
    }

    private PaymentOrder createLocalPaymentOrder(Orders order) {
        String paymentNo = IdUtil.getSnowflakeNextIdStr();

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setPaymentNo(paymentNo);
        paymentOrder.setOrderId(order.getId());
        paymentOrder.setOrderNo(order.getOrderNo());
        paymentOrder.setUserId(order.getUserId());
        paymentOrder.setPaymentChannel(PaymentChannelConstants.WECHAT_NATIVE);
        paymentOrder.setAmount(order.getPayAmount());
        paymentOrder.setStatus(PaymentStatusConstants.PAYING);
        paymentOrder.setOutTradeNo(paymentNo);
        paymentOrder.setExpireTime(order.getExpireTime());
        paymentOrderMapper.insert(paymentOrder);
        return paymentOrder;
    }

    private WechatNativePayVO buildWechatNativePayVO(Orders order, PaymentOrder paymentOrder) {
        WechatNativePayVO vo = new WechatNativePayVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPaymentNo(paymentOrder.getPaymentNo());
        vo.setOutTradeNo(paymentOrder.getOutTradeNo());
        vo.setCodeUrl(paymentOrder.getCodeUrl());
        vo.setAmount(paymentOrder.getAmount());
        vo.setExpireTime(paymentOrder.getExpireTime());
        return vo;
    }

    private void validateNotifyAmount(PaymentOrder paymentOrder, Integer providerTotalFen) {
        if (providerTotalFen == null) {
            return;
        }
        int localTotalFen = paymentOrder.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        if (localTotalFen != providerTotalFen) {
            throw new BizException(4012, "支付回调金额不一致");
        }
    }

    private void markPaymentSuccess(PaymentOrder paymentOrder, WechatPayTransactionResult transaction) {
        paymentOrder.setStatus(PaymentStatusConstants.SUCCESS);
        paymentOrder.setProviderTradeNo(transaction.providerTradeNo());
        paymentOrder.setProviderStatus(transaction.tradeState());
        paymentOrder.setNotifyPayload(transaction.rawPayload());
        paymentOrder.setSuccessTime(transaction.successTime() != null ? transaction.successTime() : LocalDateTime.now());
        paymentOrder.setErrorCode(null);
        paymentOrder.setErrorMessage(null);
        paymentOrderMapper.updateById(paymentOrder);
    }

    private void markPaymentFailed(PaymentOrder paymentOrder, String errorCode, String errorMessage) {
        paymentOrder.setStatus(PaymentStatusConstants.FAILED);
        paymentOrder.setErrorCode(errorCode);
        paymentOrder.setErrorMessage(trimErrorMessage(errorMessage));
        paymentOrderMapper.updateById(paymentOrder);
    }

    private void markPaymentAbnormal(PaymentOrder paymentOrder,
                                     WechatPayTransactionResult transaction,
                                     String errorMessage) {
        paymentOrder.setStatus(PaymentStatusConstants.ABNORMAL);
        paymentOrder.setProviderTradeNo(transaction.providerTradeNo());
        paymentOrder.setProviderStatus(transaction.tradeState());
        paymentOrder.setNotifyPayload(transaction.rawPayload());
        paymentOrder.setSuccessTime(transaction.successTime() != null ? transaction.successTime() : LocalDateTime.now());
        paymentOrder.setErrorMessage(trimErrorMessage(errorMessage));
        paymentOrderMapper.updateById(paymentOrder);
    }

    private boolean isExpired(LocalDateTime expireTime) {
        return expireTime != null && !expireTime.isAfter(LocalDateTime.now());
    }

    private String buildPaymentStatusMessage(Orders order, PaymentOrder paymentOrder) {
        if (paymentOrder == null) {
            if (order.getOrderStatus() == OrderStatusConstants.PENDING_PAYMENT) {
                return "订单待支付，尚未发起微信支付";
            }
            if (order.getOrderStatus() == OrderStatusConstants.CANCELED) {
                return "订单已取消";
            }
            return "当前订单暂无支付单";
        }

        if (paymentOrder.getStatus() == PaymentStatusConstants.SUCCESS) {
            return "支付成功";
        }
        if (paymentOrder.getStatus() == PaymentStatusConstants.CLOSED) {
            return "支付单已关闭";
        }
        if (paymentOrder.getStatus() == PaymentStatusConstants.FAILED) {
            return paymentOrder.getErrorMessage() == null ? "支付失败" : paymentOrder.getErrorMessage();
        }
        if (paymentOrder.getStatus() == PaymentStatusConstants.ABNORMAL) {
            return paymentOrder.getErrorMessage() == null ? "支付单异常，请人工核对" : paymentOrder.getErrorMessage();
        }
        if (isExpired(paymentOrder.getExpireTime())) {
            return "支付单已过期";
        }
        return "支付中";
    }

    private String trimErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        if (message.length() <= 255) {
            return message;
        }
        return message.substring(0, 255);
    }

    private WechatNativePayProvider getWechatNativePayProvider() {
        WechatNativePayProvider provider = wechatNativePayProviderObjectProvider.getIfAvailable();
        if (provider == null) {
            throw new BizException(4013, "微信支付未启用，请先完成商户配置");
        }
        return provider;
    }
}
