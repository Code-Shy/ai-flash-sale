package com.weijinchuan.aiflashsale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weijinchuan.aiflashsale.domain.Store;
import com.weijinchuan.aiflashsale.mapper.StoreMapper;
import com.weijinchuan.aiflashsale.mapper.StoreSkuMapper;
import com.weijinchuan.aiflashsale.service.StoreService;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门店服务实现类
 */
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final StoreSkuMapper storeSkuMapper;

    /**
     * 查询营业中的门店列表
     */
    @Override
    public List<Store> listStores() {
        return storeMapper.selectList(
                new LambdaQueryWrapper<Store>()
                        .eq(Store::getBusinessStatus, 1)
                        .orderByDesc(Store::getId)
        );
    }

    /**
     * 查询门店商品列表
     */
    @Override
    public List<StoreSkuVO> listStoreSkus(Long storeId) {
        return storeSkuMapper.listStoreSkus(storeId);
    }
}