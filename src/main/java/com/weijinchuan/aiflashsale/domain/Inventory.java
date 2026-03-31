package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存实体
 */
@Data
@TableName("inventory")
public class Inventory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long storeId;
    private Long skuId;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}