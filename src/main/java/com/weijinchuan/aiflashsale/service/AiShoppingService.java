package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.dto.ai.ShoppingRecommendDTO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendVO;

/**
 * AI 导购服务接口
 */
public interface AiShoppingService {

    /**
     * AI 导购推荐
     *
     * @param dto 请求参数
     * @return 推荐结果
     */
    ShoppingRecommendVO recommend(ShoppingRecommendDTO dto);
}