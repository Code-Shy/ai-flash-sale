package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体
 *
 * 说明：
 * 当前阶段采用“单门店购物车”模型，
 * 一个购物车只对应一个用户在一个门店下的商品集合。
 */
@Data
@TableName("cart")
public class Cart {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 门店 ID
     */
    private Long storeId;

    /**
     * 状态：1-有效 0-失效
     */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}