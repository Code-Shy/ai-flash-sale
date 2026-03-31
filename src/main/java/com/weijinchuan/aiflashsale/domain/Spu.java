package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SPU 实体
 */
@Data
@TableName("spu")
public class Spu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String spuName;
    private String categoryName;
    private String brandName;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}