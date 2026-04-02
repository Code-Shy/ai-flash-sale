package com.weijinchuan.aiflashsale.service.impl;

import com.weijinchuan.aiflashsale.domain.AiMessage;
import com.weijinchuan.aiflashsale.service.ClarificationStrategyService;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 追问策略实现
 */
@Service
public class ClarificationStrategyServiceImpl implements ClarificationStrategyService {

    @Value("${ai-shopping.clarification.enabled:true}")
    private boolean clarificationEnabled;

    @Value("${ai-shopping.clarification.short-query-length:8}")
    private int shortQueryLength;

    @Value("${ai-shopping.clarification.recommendation-threshold:2}")
    private int recommendationThreshold;

    @Override
    public String buildFollowUpQuestion(String query,
                                        ShoppingIntentVO intent,
                                        List<ShoppingRecommendItemVO> recommendations,
                                        List<String> preferenceHints,
                                        List<AiMessage> recentMessages) {
        if (!clarificationEnabled) {
            return null;
        }

        int recommendationSize = recommendations == null ? 0 : recommendations.size();
        boolean missingCategory = isBlank(intent.getCategoryKeyword()) && isBlank(intent.getProductKeyword());
        boolean missingScene = isBlank(intent.getSceneKeyword());
        boolean missingTaste = isBlank(intent.getTastePreference());
        boolean missingBudget = intent.getBudget() == null;
        boolean shortQuery = query == null || query.trim().length() <= shortQueryLength;

        String preferencePrefix = buildPreferencePrefix(preferenceHints);

        if (recommendationSize == 0) {
            if (!missingCategory || !missingBudget || !missingScene || !missingTaste) {
                return preferencePrefix + "当前条件下还没有筛到特别合适的商品，你可以告诉我更偏向哪类商品，或者是否接受放宽预算和口味限制。";
            }
            return preferencePrefix + "你这次更想买咖啡、轻食还是热食？预算大概多少？";
        }

        if (shortQuery && missingCategory && missingBudget) {
            return preferencePrefix + "你这次更偏咖啡、轻食还是热食？预算大概多少，我可以继续帮你缩小范围。";
        }

        if (missingCategory && recommendationSize < recommendationThreshold) {
            return preferencePrefix + "你更想要咖啡、轻食还是热食？告诉我品类后，我可以把推荐收得更准。";
        }

        if (!missingCategory && missingScene && missingTaste && recommendationSize <= recommendationThreshold) {
            return preferencePrefix + "这次更偏早餐、夜宵还是加班补给？口味上要清淡、不辣，还是提神一点？";
        }

        if (missingBudget && recommendationSize <= recommendationThreshold && !containsBudgetQuestion(recentMessages)) {
            return preferencePrefix + "如果你愿意，也可以告诉我预算区间，我可以顺便帮你控制价格。";
        }

        return null;
    }

    private boolean containsBudgetQuestion(List<AiMessage> recentMessages) {
        if (recentMessages == null || recentMessages.isEmpty()) {
            return false;
        }
        for (int i = recentMessages.size() - 1; i >= 0 && i >= recentMessages.size() - 2; i--) {
            String content = recentMessages.get(i).getContent();
            if (!isBlank(content) && content.contains("预算")) {
                return true;
            }
        }
        return false;
    }

    private String buildPreferencePrefix(List<String> preferenceHints) {
        if (preferenceHints == null || preferenceHints.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("我会先参考你最近的");
        int size = Math.min(2, preferenceHints.size());
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append("、");
            }
            builder.append(preferenceHints.get(i).replace("：", ""));
        }
        builder.append("。");
        return builder.toString();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
