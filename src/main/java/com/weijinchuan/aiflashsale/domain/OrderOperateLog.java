package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单操作日志实体
 */
@Data
@TableName("order_operate_log")
public class OrderOperateLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    private String orderNo;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String operateType;
    private String operateBy;
    private String remark;
    private LocalDateTime createTime;
}