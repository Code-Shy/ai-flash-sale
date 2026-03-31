package com.weijinchuan.aiflashsale.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店商品返回对象
 */
@Data
public class StoreSkuVO {

    private Long storeId;
    private Long skuId;
    private String skuName;
    private String specs;
    private String imageUrl;
    private BigDecimal salePrice;
    private Integer availableStock;
}