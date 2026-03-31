package com.weijinchuan.aiflashsale.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改购物车项请求对象
 */
@Data
public class UpdateCartItemDTO {

    /**
     * 新的购买数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于等于 1")
    private Integer quantity;
}