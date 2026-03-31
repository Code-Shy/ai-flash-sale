package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项实体
 */
@Data
@TableName("cart_item")
public class CartItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 购物车 ID
     */
    private Long cartId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 门店 ID
     */
    private Long storeId;

    /**
     * 商品 SKU ID
     */
    private Long skuId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 是否选中：1-选中 0-未选中
     */
    private Integer checked;

    /**
     * 加入购物车时的价格快照
     */
    private BigDecimal priceSnapshot;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}