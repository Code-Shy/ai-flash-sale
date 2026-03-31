package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * 说明：
 * 这里不用 Order 命名，是为了避免和 SQL 关键字冲突。
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 门店 ID
     */
    private Long storeId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 配送费
     */
    private BigDecimal deliveryFee;

    /**
     * 订单状态：
     * 10-待支付 20-已支付 30-已取消 40-已完成
     */
    private Integer orderStatus;

    /**
     * 用户备注
     */
    private String remark;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}