package com.weijinchuan.aiflashsale.service.llm;

import com.weijinchuan.aiflashsale.service.rag.RetrievedKnowledge;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;

import java.util.List;

/**
 * AI 导购生成器
 */
public interface ShoppingGenerationProvider {

    /**
     * 生成商品推荐理由
     */
    String generateRecommendationReason(String query,
                                        ShoppingIntentVO intent,
                                        StoreSkuVO product,
                                        List<RetrievedKnowledge> references);

    /**
     * 生成推荐总结
     */
    String generateRecommendationSummary(String query,
                                         ShoppingIntentVO intent,
                                         List<ShoppingRecommendItemVO> recommendations);

    /**
     * 生成带上下文的推荐总结
     */
    default String generateRecommendationSummary(String query,
                                                 ShoppingIntentVO intent,
                                                 List<ShoppingRecommendItemVO> recommendations,
                                                 String conversationContext,
                                                 List<String> preferenceHints) {
        return generateRecommendationSummary(query, intent, recommendations);
    }

    /**
     * 生成问答回答
     */
    String generateAnswer(String query,
                          ShoppingIntentVO intent,
                          List<ShoppingRecommendItemVO> recommendations,
                          List<RetrievedKnowledge> references);

    /**
     * 生成带上下文的问答回答
     */
    default String generateAnswer(String query,
                                  ShoppingIntentVO intent,
                                  List<ShoppingRecommendItemVO> recommendations,
                                  List<RetrievedKnowledge> references,
                                  String conversationContext,
                                  List<String> preferenceHints) {
        return generateAnswer(query, intent, recommendations, references);
    }
}
