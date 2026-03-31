package com.weijinchuan.aiflashsale.service.llm;

import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;

/**
 * 意图解析 Provider
 *
 * 用于把自然语言购物需求解析成结构化对象。
 */
public interface IntentParseProvider {

    /**
     * 解析购物意图
     *
     * @param query 用户自然语言输入
     * @return 结构化购物意图
     */
    ShoppingIntentVO parseShoppingIntent(String query);
}