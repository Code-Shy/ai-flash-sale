package com.weijinchuan.aiflashsale.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.common.constant.RedisKeyConstants;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.Store;
import com.weijinchuan.aiflashsale.mapper.StoreMapper;
import com.weijinchuan.aiflashsale.mapper.StoreSkuMapper;
import com.weijinchuan.aiflashsale.service.StoreService;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 门店服务实现类
 */
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final StoreSkuMapper storeSkuMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

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
     *
     * 先查缓存，未命中再查数据库
     */
    @Override
    public List<StoreSkuVO> listStoreSkus(Long storeId) {
        String cacheKey = RedisKeyConstants.STORE_SKU_KEY_PREFIX + storeId;

        try {
            String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

            if (StrUtil.isNotBlank(cacheValue)) {
                return objectMapper.readValue(cacheValue, new TypeReference<List<StoreSkuVO>>() {});
            }

            List<StoreSkuVO> storeSkus = storeSkuMapper.listStoreSkus(storeId);

            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(storeSkus),
                    30,
                    TimeUnit.MINUTES
            );

            return storeSkus;
        } catch (Exception e) {
            throw new BizException(5001, "查询门店商品列表失败");
        }
    }
}