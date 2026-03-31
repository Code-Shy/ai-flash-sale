package com.weijinchuan.aiflashsale.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.common.constant.RedisKeyConstants;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.mapper.StoreSkuMapper;
import com.weijinchuan.aiflashsale.service.ProductService;
import com.weijinchuan.aiflashsale.vo.ProductDetailVO;
import com.weijinchuan.aiflashsale.vo.ProductListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final StoreSkuMapper storeSkuMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 查询商品列表
     *
     * 逻辑：
     * 1. 先查 Redis
     * 2. 命中则直接返回
     * 3. 未命中则查数据库并写回缓存
     */
    @Override
    public List<ProductListVO> listProducts() {
        try {
            String cacheValue = stringRedisTemplate.opsForValue()
                    .get(RedisKeyConstants.PRODUCT_LIST_KEY);

            if (StrUtil.isNotBlank(cacheValue)) {
                return objectMapper.readValue(cacheValue, new TypeReference<List<ProductListVO>>() {});
            }

            List<ProductListVO> products = storeSkuMapper.listProducts();

            stringRedisTemplate.opsForValue().set(
                    RedisKeyConstants.PRODUCT_LIST_KEY,
                    objectMapper.writeValueAsString(products),
                    30,
                    TimeUnit.MINUTES
            );

            return products;
        } catch (Exception e) {
            throw new BizException(5001, "查询商品列表失败");
        }
    }

    /**
     * 查询商品详情
     */
    @Override
    public ProductDetailVO getProductDetail(Long skuId) {
        String cacheKey = RedisKeyConstants.PRODUCT_DETAIL_KEY_PREFIX + skuId;

        try {
            String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

            if (StrUtil.isNotBlank(cacheValue)) {
                return objectMapper.readValue(cacheValue, ProductDetailVO.class);
            }

            ProductDetailVO detail = storeSkuMapper.getProductDetail(skuId);
            if (detail == null) {
                throw new BizException(404, "商品不存在");
            }

            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(detail),
                    30,
                    TimeUnit.MINUTES
            );

            return detail;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(5001, "查询商品详情失败");
        }
    }
}