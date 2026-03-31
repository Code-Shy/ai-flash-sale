package com.weijinchuan.aiflashsale.service.tool;

import com.weijinchuan.aiflashsale.service.StoreService;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 门店商品查询工具
 */
@Component
@RequiredArgsConstructor
public class StoreProductQueryTool {

    private final StoreService storeService;

    /**
     * 查询某个门店下的商品列表
     */
    public List<StoreSkuVO> listStoreProducts(Long storeId) {
        return storeService.listStoreSkus(storeId);
    }
}