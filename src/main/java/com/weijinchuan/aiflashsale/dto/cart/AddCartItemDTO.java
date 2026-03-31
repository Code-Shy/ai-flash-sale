package com.weijinchuan.aiflashsale.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车请求对象
 */
@Data
public class AddCartItemDTO {

    /**
     * 用户 ID
     * 当前项目先写死从前端传，后面再升级为登录态获取
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 门店 ID
     */
    @NotNull(message = "门店 ID 不能为空")
    private Long storeId;

    /**
     * 商品 SKU ID
     */
    @NotNull(message = "商品 SKU ID 不能为空")
    private Long skuId;

    /**
     * 购买数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于等于 1")
    private Integer quantity;
}