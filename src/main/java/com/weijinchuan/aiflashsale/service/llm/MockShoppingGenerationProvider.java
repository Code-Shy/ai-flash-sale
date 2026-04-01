package com.weijinchuan.aiflashsale.service.llm;

import com.weijinchuan.aiflashsale.service.rag.RetrievedKnowledge;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本地模板版导购生成器
 */
@Component
public class MockShoppingGenerationProvider implements ShoppingGenerationProvider {

    @Override
    public String generateRecommendationReason(String query,
                                               ShoppingIntentVO intent,
                                               StoreSkuVO product,
                                               List<RetrievedKnowledge> references) {
        List<String> reasons = new ArrayList<>();

        if (intent.getBudget() != null
                && product.getSalePrice() != null
                && product.getSalePrice().compareTo(BigDecimal.valueOf(intent.getBudget())) <= 0) {
            reasons.add("价格在预算内");
        }

        if (!isBlank(intent.getSceneKeyword())) {
            reasons.add("适合" + intent.getSceneKeyword() + "场景");
        }

        if (!isBlank(intent.getTastePreference())) {
            reasons.add("更贴合" + intent.getTastePreference() + "口味偏好");
        }

        if (!references.isEmpty()) {
            reasons.add("可参考：" + references.get(0).getDocument().getTitle());
        }

        if (reasons.isEmpty()) {
            reasons.add("和当前需求较匹配");
        }

        return String.join("，", reasons);
    }

    @Override
    public String generateRecommendationSummary(String query,
                                                ShoppingIntentVO intent,
                                                List<ShoppingRecommendItemVO> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "当前门店里没有找到特别匹配的商品，可以放宽预算、口味或场景条件再试。";
        }

        String topNames = recommendations.stream()
                .limit(2)
                .map(ShoppingRecommendItemVO::getSkuName)
                .collect(Collectors.joining("、"));

        StringBuilder summary = new StringBuilder("结合你的需求，优先可以看");
        summary.append(topNames).append("。");

        if (!isBlank(intent.getSceneKeyword())) {
            summary.append("这次推荐会优先考虑").append(intent.getSceneKeyword()).append("场景。");
        }
        if (!isBlank(intent.getTastePreference())) {
            summary.append("同时会尽量贴合").append(intent.getTastePreference()).append("口味偏好。");
        }

        return summary.toString();
    }

    @Override
    public String generateAnswer(String query,
                                 ShoppingIntentVO intent,
                                 List<ShoppingRecommendItemVO> recommendations,
                                 List<RetrievedKnowledge> references) {
        StringBuilder answer = new StringBuilder();

        if (references != null && !references.isEmpty()) {
            answer.append("结合门店知识库信息，")
                    .append(references.get(0).getSnippet())
                    .append("。");
        } else {
            answer.append("当前知识库里没有特别直接的说明，");
        }

        if (recommendations != null && !recommendations.isEmpty()) {
            String productNames = recommendations.stream()
                    .limit(3)
                    .map(ShoppingRecommendItemVO::getSkuName)
                    .collect(Collectors.joining("、"));
            answer.append("你可以优先关注").append(productNames).append("。");
        } else {
            answer.append("当前门店商品里没有筛到特别贴合的问题相关商品。");
        }

        if (!isBlank(intent.getSceneKeyword())) {
            answer.append("我会继续按").append(intent.getSceneKeyword()).append("场景理解你的需求。");
        }

        return answer.toString();
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
