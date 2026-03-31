package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.domain.Store;
import com.weijinchuan.aiflashsale.service.StoreService;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店接口控制器
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 门店列表
     */
    @GetMapping
    @Operation(summary = "门店列表")
    public Result<List<Store>> listStores() {
        return Result.success(storeService.listStores());
    }

    /**
     * 门店商品列表
     */
    @GetMapping("/{storeId}/skus")
    @Operation(summary = "门店商品列表")
    public Result<List<StoreSkuVO>> listStoreSkus(@PathVariable Long storeId) {
        return Result.success(storeService.listStoreSkus(storeId));
    }
}