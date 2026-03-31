package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情返回对象
 */
@Data
public class OrderDetailVO {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long storeId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal deliveryFee;
    private Integer orderStatus;
    private String remark;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}