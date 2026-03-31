package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店实体
 */
@Data
@TableName("store")
public class Store {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店类型
     */
    private String storeType;

    /**
     * 门店地址
     */
    private String address;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 配送半径（公里）
     */
    private BigDecimal deliveryRadiusKm;

    /**
     * 营业状态：1-营业中，0-休息
     */
    private Integer businessStatus;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}