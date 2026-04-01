package com.weijinchuan.aiflashsale.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 微信 Native 预下单请求
 */
@Data
public class WechatNativePayDTO {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;
}
