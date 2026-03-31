package com.weijinchuan.aiflashsale.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交订单请求对象
 */
@Data
public class SubmitOrderDTO {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 门店 ID
     */
    @NotNull(message = "门店 ID 不能为空")
    private Long storeId;

    /**
     * 备注
     */
    private String remark;
}