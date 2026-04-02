package com.weijinchuan.aiflashsale.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 导购问答请求对象
 */
@Data
public class ShoppingAskDTO {

    /**
     * 会话 ID，传空表示开启新会话
     */
    private Long sessionId;

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
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String query;
}
