package com.weijinchuan.aiflashsale.service.llm;

import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import org.springframework.stereotype.Component;

/**
 * 本地规则版意图解析器
 *
 * 说明：
 * 1. 当通义 API Key 未配置时使用
 * 2. 当调用通义失败时作为兜底
 */
@Component
public class MockIntentParseProvider implements IntentParseProvider {

    @Override
    public ShoppingIntentVO parseShoppingIntent(String query) {
        ShoppingIntentVO intent = new ShoppingIntentVO();
        intent.setRawQuery(query);

        // 提取预算
        Integer budget = extractBudget(query);
        intent.setBudget(budget);

        // 场景解析
        if (query.contains("晚上") || query.contains("夜宵")) {
            intent.setSceneKeyword("夜宵");
        } else if (query.contains("早餐")) {
            intent.setSceneKeyword("早餐");
        } else if (query.contains("加班")) {
            intent.setSceneKeyword("加班");
        }

        // 口味偏好解析
        if (query.contains("不辣")) {
            intent.setTastePreference("不辣");
        } else if (query.contains("不太油") || query.contains("清淡")) {
            intent.setTastePreference("清淡");
        }

        // 品类 / 商品解析
        if (query.contains("咖啡") || query.contains("美式")) {
            intent.setCategoryKeyword("咖啡");
            intent.setProductKeyword("美式");
        } else if (query.contains("轻食") || query.contains("三明治")) {
            intent.setCategoryKeyword("轻食");
            intent.setProductKeyword("三明治");
        } else if (query.contains("热食") || query.contains("关东煮")) {
            intent.setCategoryKeyword("热食");
            intent.setProductKeyword("关东煮");
        } else if (query.contains("吃")) {
            intent.setCategoryKeyword("热食");
        }

        return intent;
    }

    /**
     * 提取“xx元”预算
     */
    private Integer extractBudget(String query) {
        String normalized = query.replace("块钱", "元").replace("块", "元");
        int yuanIndex = normalized.indexOf("元");
        if (yuanIndex <= 0) {
            return null;
        }

        int start = yuanIndex - 1;
        while (start >= 0 && Character.isDigit(normalized.charAt(start))) {
            start--;
        }

        String number = normalized.substring(start + 1, yuanIndex);
        if (number.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return null;
        }
    }
}