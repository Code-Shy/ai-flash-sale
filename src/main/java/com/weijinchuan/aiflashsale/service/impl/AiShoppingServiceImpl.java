package com.weijinchuan.aiflashsale.service.impl;

import com.weijinchuan.aiflashsale.dto.ai.ShoppingRecommendDTO;
import com.weijinchuan.aiflashsale.service.AiShoppingService;
import com.weijinchuan.aiflashsale.service.llm.IntentParseProvider;
import com.weijinchuan.aiflashsale.service.tool.StoreProductQueryTool;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 导购服务实现类
 *
 * 核心流程：
 * 1. 解析自然语言
 * 2. 调用工具查询门店商品
 * 3. 按预算 / 关键词过滤
 * 4. 返回推荐结果和推荐理由
 */
@Service
@RequiredArgsConstructor
public class AiShoppingServiceImpl implements AiShoppingService {

    private final IntentParseProvider intentParseProvider;
    private final StoreProductQueryTool storeProductQueryTool;

    @Override
    public ShoppingRecommendVO recommend(ShoppingRecommendDTO dto) {
        // 第一步：解析购物意图
        ShoppingIntentVO intent = intentParseProvider.parseShoppingIntent(dto.getQuery());

        // 第二步：查询门店商品
        List<StoreSkuVO> storeProducts = storeProductQueryTool.listStoreProducts(dto.getStoreId());

        // 第三步：构建推荐结果
        List<ShoppingRecommendItemVO> recommendations = buildRecommendations(intent, storeProducts);

        // 第四步：组装返回
        ShoppingRecommendVO result = new ShoppingRecommendVO();
        result.setQuery(dto.getQuery());
        result.setIntent(intent);
        result.setRecommendations(recommendations);
        return result;
    }

    /**
     * 构建推荐列表
     */
    private List<ShoppingRecommendItemVO> buildRecommendations(ShoppingIntentVO intent,
                                                               List<StoreSkuVO> products) {
        List<ShoppingRecommendItemVO> result = new ArrayList<>();

        for (StoreSkuVO product : products) {
            // 1. 按预算过滤
            if (intent.getBudget() != null) {
                if (product.getSalePrice() == null
                        || product.getSalePrice().compareTo(BigDecimal.valueOf(intent.getBudget())) > 0) {
                    continue;
                }
            }

            // 2. 按意图过滤
            if (!matchIntent(intent, product)) {
                continue;
            }

            // 3. 组装推荐项
            ShoppingRecommendItemVO item = new ShoppingRecommendItemVO();
            item.setSkuId(product.getSkuId());
            item.setSkuName(product.getSkuName());
            item.setSpecs(product.getSpecs());
            item.setImageUrl(product.getImageUrl());
            item.setSalePrice(product.getSalePrice());
            item.setReason(buildReason(intent, product));

            result.add(item);

            // 最多返回 5 条
            if (result.size() >= 5) {
                break;
            }
        }

        return result;
    }

    /**
     * 判断商品是否符合用户意图
     */
    private boolean matchIntent(ShoppingIntentVO intent, StoreSkuVO product) {
        String text = (product.getSkuName() == null ? "" : product.getSkuName())
                + " "
                + (product.getSpecs() == null ? "" : product.getSpecs());

        // 没有关键词时默认通过
        if (isBlank(intent.getCategoryKeyword())
                && isBlank(intent.getProductKeyword())
                && isBlank(intent.getTastePreference())) {
            return true;
        }

        // 商品关键词优先
        if (!isBlank(intent.getProductKeyword()) && text.contains(intent.getProductKeyword())) {
            return true;
        }

        // 口味匹配
        if (!isBlank(intent.getTastePreference())) {
            if ("不辣".equals(intent.getTastePreference()) && !text.contains("辣")) {
                return true;
            }
            if ("清淡".equals(intent.getTastePreference())
                    && (text.contains("三明治") || text.contains("鸡胸肉") || text.contains("美式"))) {
                return true;
            }
        }

        // 品类兜底匹配
        if (!isBlank(intent.getCategoryKeyword())) {
            if ("咖啡".equals(intent.getCategoryKeyword()) && text.contains("美式")) {
                return true;
            }
            if ("轻食".equals(intent.getCategoryKeyword()) && text.contains("三明治")) {
                return true;
            }
            if ("热食".equals(intent.getCategoryKeyword()) && text.contains("关东煮")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 构建推荐理由
     */
    private String buildReason(ShoppingIntentVO intent, StoreSkuVO product) {
        List<String> reasons = new ArrayList<>();

        if (intent.getBudget() != null && product.getSalePrice() != null) {
            reasons.add("价格在预算内");
        }

        if (!isBlank(intent.getSceneKeyword()) && "夜宵".equals(intent.getSceneKeyword())) {
            reasons.add("适合夜宵场景");
        }

        if (!isBlank(intent.getTastePreference())) {
            reasons.add("符合你的口味偏好");
        }

        if (reasons.isEmpty()) {
            reasons.add("与当前需求较匹配");
        }

        return String.join("，", reasons);
    }

    /**
     * 判空工具方法
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}