package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.domain.AiMessage;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;

import java.util.List;

/**
 * AI 追问策略服务
 */
public interface ClarificationStrategyService {

    /**
     * 当信息不充分时生成追问问题。
     *
     * @return 返回 null 表示不需要追问。
     */
    String buildFollowUpQuestion(String query,
                                 ShoppingIntentVO intent,
                                 List<ShoppingRecommendItemVO> recommendations,
                                 List<String> preferenceHints,
                                 List<AiMessage> recentMessages);
}
