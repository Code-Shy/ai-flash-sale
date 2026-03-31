package com.weijinchuan.aiflashsale.service.tool;

import com.weijinchuan.aiflashsale.service.ProductService;
import com.weijinchuan.aiflashsale.vo.ProductListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品查询工具
 *
 * 当前 Day5 主要使用门店商品工具，
 * 这个类用于后续扩展全局商品召回。
 */
@Component
@RequiredArgsConstructor
public class ProductQueryTool {

    private final ProductService productService;

    /**
     * 查询全局商品列表
     */
    public List<ProductListVO> listProducts() {
        return productService.listProducts();
    }
}