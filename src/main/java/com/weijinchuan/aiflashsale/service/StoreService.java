package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.domain.Store;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;

import java.util.List;

/**
 * 门店服务接口
 */
public interface StoreService {

    /**
     * 查询营业中的门店列表
     */
    List<Store> listStores();

    /**
     * 查询门店商品列表
     */
    List<StoreSkuVO> listStoreSkus(Long storeId);
}