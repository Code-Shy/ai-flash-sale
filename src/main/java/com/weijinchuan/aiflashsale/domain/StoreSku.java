package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店商品实体
 *
 * 即时零售里最关键的一张表：
 * 同一个 SKU 在不同门店可以有不同售价和售卖状态
 */
@Data
@TableName("store_sku")
public class StoreSku {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long storeId;
    private Long skuId;
    private BigDecimal originPrice;
    private BigDecimal salePrice;
    private Integer saleStatus;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}