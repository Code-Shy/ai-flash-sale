package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.service.ProductService;
import com.weijinchuan.aiflashsale.vo.ProductDetailVO;
import com.weijinchuan.aiflashsale.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口控制器
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 商品列表
     */
    @GetMapping
    @Operation(summary = "商品列表")
    public Result<List<ProductListVO>> listProducts() {
        return Result.success(productService.listProducts());
    }

    /**
     * 商品详情
     */
    @GetMapping("/{skuId}")
    @Operation(summary = "商品详情")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long skuId) {
        return Result.success(productService.getProductDetail(skuId));
    }
}