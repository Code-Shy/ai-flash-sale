package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.vo.ProductDetailVO;
import com.weijinchuan.aiflashsale.vo.ProductListVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 查询商品列表
     */
    List<ProductListVO> listProducts();

    /**
     * 查询商品详情
     */
    ProductDetailVO getProductDetail(Long skuId);
}