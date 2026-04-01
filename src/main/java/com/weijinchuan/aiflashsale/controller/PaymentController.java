package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.dto.payment.WechatNativePayDTO;
import com.weijinchuan.aiflashsale.service.PaymentService;
import com.weijinchuan.aiflashsale.vo.payment.PaymentStatusVO;
import com.weijinchuan.aiflashsale.vo.payment.WechatNativePayVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 支付接口控制器
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/wechat/native/prepay")
    @Operation(summary = "发起微信 Native 支付")
    public Result<WechatNativePayVO> createWechatNativePay(@Valid @RequestBody WechatNativePayDTO dto) {
        return Result.success(paymentService.createWechatNativePay(dto));
    }

    @GetMapping("/orders/{orderId}/status")
    @Operation(summary = "查询订单支付状态")
    public Result<PaymentStatusVO> getPaymentStatus(@RequestParam Long userId,
                                                    @PathVariable Long orderId) {
        return Result.success(paymentService.getPaymentStatus(userId, orderId));
    }

    @PostMapping("/wechat/native/notify")
    @Operation(summary = "微信支付回调")
    public ResponseEntity<String> handleWechatNativePayNotify(
            @RequestBody String body,
            @RequestHeader("Wechatpay-Serial") String serialNumber,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Timestamp") String timestamp) {
        try {
            paymentService.handleWechatNativePayNotify(body, serialNumber, nonce, signature, timestamp);
            return ResponseEntity.ok("success");
        } catch (BizException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
