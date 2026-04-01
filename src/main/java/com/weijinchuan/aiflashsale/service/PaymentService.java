package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.dto.payment.WechatNativePayDTO;
import com.weijinchuan.aiflashsale.vo.payment.PaymentStatusVO;
import com.weijinchuan.aiflashsale.vo.payment.WechatNativePayVO;

/**
 * 支付服务
 */
public interface PaymentService {

    /**
     * 发起微信 Native 支付
     */
    WechatNativePayVO createWechatNativePay(WechatNativePayDTO dto);

    /**
     * 查询订单支付状态
     */
    PaymentStatusVO getPaymentStatus(Long userId, Long orderId);

    /**
     * 处理微信支付回调
     */
    void handleWechatNativePayNotify(String body,
                                     String serialNumber,
                                     String nonce,
                                     String signature,
                                     String timestamp);
}
