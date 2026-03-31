package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SKU 实体
 */
@Data
@TableName("sku")
public class Sku {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long spuId;
    private String skuName;
    private String specs;
    private String unit;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}