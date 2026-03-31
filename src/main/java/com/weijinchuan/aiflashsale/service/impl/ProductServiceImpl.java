package com.weijinchuan.aiflashsale.service.impl;

import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.mapper.StoreSkuMapper;
import com.weijinchuan.aiflashsale.service.ProductService;
import com.weijinchuan.aiflashsale.vo.ProductDetailVO;
import com.weijinchuan.aiflashsale.vo.ProductListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final StoreSkuMapper storeSkuMapper;

    /**
     * 查询商品列表
     */
    @Override
    public List<ProductListVO> listProducts() {
        return storeSkuMapper.listProducts();
    }

    /**
     * 查询商品详情
     */
    @Override
    public ProductDetailVO getProductDetail(Long skuId) {
        ProductDetailVO detail = storeSkuMapper.getProductDetail(skuId);
        if (detail == null) {
            throw new BizException(404, "商品不存在");
        }
        return detail;
    }
}