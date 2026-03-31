package com.weijinchuan.aiflashsale.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 导购请求对象
 */
@Data
public class ShoppingRecommendDTO {

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
     * 用户自然语言购物需求
     */
    @NotBlank(message = "购物需求不能为空")
    private String query;
}