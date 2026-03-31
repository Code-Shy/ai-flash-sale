package com.weijinchuan.aiflashsale.common.constant;

/**
 * Redis Key 常量类
 *
 * 作用：
 * 1. 统一管理 Redis Key
 * 2. 避免在代码里到处写魔法字符串
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    /**
     * 商品列表缓存 Key
     */
    public static final String PRODUCT_LIST_KEY = "product:list";

    /**
     * 商品详情缓存前缀
     */
    public static final String PRODUCT_DETAIL_KEY_PREFIX = "product:detail:";

    /**
     * 门店商品列表缓存前缀
     */
    public static final String STORE_SKU_KEY_PREFIX = "store:skus:";

    /**
     * 下单幂等 token 前缀
     */
    public static final String ORDER_SUBMIT_TOKEN_PREFIX = "order:submit:token:";
}