package com.weijinchuan.aiflashsale.service.impl;

import com.weijinchuan.aiflashsale.dto.ai.ShoppingAskDTO;
import com.weijinchuan.aiflashsale.dto.ai.ShoppingRecommendDTO;
import com.weijinchuan.aiflashsale.service.AiShoppingService;
import com.weijinchuan.aiflashsale.service.llm.IntentParseProvider;
import com.weijinchuan.aiflashsale.service.llm.ShoppingGenerationProvider;
import com.weijinchuan.aiflashsale.service.rag.KnowledgeRetriever;
import com.weijinchuan.aiflashsale.service.rag.RetrievedKnowledge;
import com.weijinchuan.aiflashsale.service.tool.StoreProductQueryTool;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import com.weijinchuan.aiflashsale.vo.ai.KnowledgeReferenceVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingAnswerVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * AI 导购服务实现类
 *
 * 核心流程：
 * 1. 解析自然语言
 * 2. 调用工具查询门店商品
 * 3. 用规则做第一轮筛选
 * 4. 用知识检索补充上下文
 * 5. 调用生成器返回推荐结果 / 问答结果
 */
@Service
@RequiredArgsConstructor
public class AiShoppingServiceImpl implements AiShoppingService {

    private final IntentParseProvider intentParseProvider;
    private final StoreProductQueryTool storeProductQueryTool;
    private final KnowledgeRetriever knowledgeRetriever;
    private final ShoppingGenerationProvider shoppingGenerationProvider;

    @Override
    public ShoppingRecommendVO recommend(ShoppingRecommendDTO dto) {
        ShoppingIntentVO intent = intentParseProvider.parseShoppingIntent(dto.getQuery());
        List<StoreSkuVO> storeProducts = storeProductQueryTool.listStoreProducts(dto.getStoreId());
        List<ShoppingRecommendItemVO> recommendations = buildRecommendations(
                dto.getStoreId(),
                dto.getQuery(),
                intent,
                storeProducts,
                5
        );

        ShoppingRecommendVO result = new ShoppingRecommendVO();
        result.setQuery(dto.getQuery());
        result.setIntent(intent);
        result.setSummary(shoppingGenerationProvider.generateRecommendationSummary(
                dto.getQuery(),
                intent,
                recommendations
        ));
        result.setRecommendations(recommendations);
        return result;
    }

    @Override
    public ShoppingAnswerVO ask(ShoppingAskDTO dto) {
        ShoppingIntentVO intent = intentParseProvider.parseShoppingIntent(dto.getQuery());
        List<StoreSkuVO> storeProducts = storeProductQueryTool.listStoreProducts(dto.getStoreId());
        List<ShoppingRecommendItemVO> recommendations = buildRecommendations(
                dto.getStoreId(),
                dto.getQuery(),
                intent,
                storeProducts,
                3
        );
        List<RetrievedKnowledge> references = knowledgeRetriever.search(
                dto.getStoreId(),
                null,
                dto.getQuery(),
                4
        );

        ShoppingAnswerVO answerVO = new ShoppingAnswerVO();
        answerVO.setQuery(dto.getQuery());
        answerVO.setIntent(intent);
        answerVO.setAnswer(shoppingGenerationProvider.generateAnswer(
                dto.getQuery(),
                intent,
                recommendations,
                references
        ));
        answerVO.setRecommendations(recommendations);
        answerVO.setReferences(toKnowledgeReferenceVOs(references));
        return answerVO;
    }

    /**
     * 构建推荐列表
     */
    private List<ShoppingRecommendItemVO> buildRecommendations(Long storeId,
                                                               String query,
                                                               ShoppingIntentVO intent,
                                                               List<StoreSkuVO> products,
                                                               int limit) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        List<ShoppingRecommendItemVO> result = new ArrayList<>();
        List<StoreSkuVO> rankedProducts = new ArrayList<>(products);
        rankedProducts.sort(Comparator
                .comparingInt((StoreSkuVO product) -> scoreProduct(product, intent, query))
                .reversed()
                .thenComparing(StoreSkuVO::getSalePrice, Comparator.nullsLast(Comparator.naturalOrder())));

        for (StoreSkuVO product : rankedProducts) {
            if (scoreProduct(product, intent, query) <= 0) {
                continue;
            }

            List<RetrievedKnowledge> references = knowledgeRetriever.search(
                    storeId,
                    product.getSkuId(),
                    buildRetrievalQuery(query, intent, product),
                    2
            );

            ShoppingRecommendItemVO item = new ShoppingRecommendItemVO();
            item.setSkuId(product.getSkuId());
            item.setSkuName(product.getSkuName());
            item.setSpecs(product.getSpecs());
            item.setImageUrl(product.getImageUrl());
            item.setSalePrice(product.getSalePrice());
            item.setReason(shoppingGenerationProvider.generateRecommendationReason(
                    query,
                    intent,
                    product,
                    references
            ));
            item.setReferences(toKnowledgeReferenceVOs(references));

            result.add(item);

            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    /**
     * 计算商品和当前需求的匹配分
     */
    private int scoreProduct(StoreSkuVO product, ShoppingIntentVO intent, String query) {
        if (intent.getBudget() != null) {
            if (product.getSalePrice() == null
                    || product.getSalePrice().compareTo(BigDecimal.valueOf(intent.getBudget())) > 0) {
                return -1;
            }
        }

        int score = 0;
        String text = normalize(safeText(product.getSkuName()) + " " + safeText(product.getSpecs()));

        if (product.getAvailableStock() != null && product.getAvailableStock() > 0) {
            score += 2;
        }

        if (matchIntent(intent, product)) {
            score += 12;
        }

        for (String term : extractTerms(query)) {
            if (term.length() >= 2 && text.contains(term)) {
                score += 3;
            }
        }

        if (isBlank(intent.getCategoryKeyword())
                && isBlank(intent.getProductKeyword())
                && isBlank(intent.getTastePreference())
                && score == 0) {
            score = 1;
        }

        return score;
    }

    /**
     * 判断商品是否符合用户意图
     */
    private boolean matchIntent(ShoppingIntentVO intent, StoreSkuVO product) {
        String text = safeText(product.getSkuName()) + " " + safeText(product.getSpecs());

        if (isBlank(intent.getCategoryKeyword())
                && isBlank(intent.getProductKeyword())
                && isBlank(intent.getTastePreference())) {
            return true;
        }

        if (!isBlank(intent.getProductKeyword()) && text.contains(intent.getProductKeyword())) {
            return true;
        }

        if (!isBlank(intent.getTastePreference())) {
            if ("不辣".equals(intent.getTastePreference()) && !text.contains("辣")) {
                return true;
            }
            if ("清淡".equals(intent.getTastePreference())
                    && (text.contains("三明治") || text.contains("鸡胸肉") || text.contains("美式"))) {
                return true;
            }
        }

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

    private String buildRetrievalQuery(String query, ShoppingIntentVO intent, StoreSkuVO product) {
        StringBuilder builder = new StringBuilder(query);
        if (!isBlank(product.getSkuName())) {
            builder.append(" ").append(product.getSkuName());
        }
        if (!isBlank(product.getSpecs())) {
            builder.append(" ").append(product.getSpecs());
        }
        if (!isBlank(intent.getSceneKeyword())) {
            builder.append(" ").append(intent.getSceneKeyword());
        }
        return builder.toString();
    }

    private List<KnowledgeReferenceVO> toKnowledgeReferenceVOs(List<RetrievedKnowledge> references) {
        List<KnowledgeReferenceVO> result = new ArrayList<>();
        for (RetrievedKnowledge reference : references) {
            KnowledgeReferenceVO item = new KnowledgeReferenceVO();
            item.setTitle(reference.getDocument().getTitle());
            item.setDocType(reference.getDocument().getDocType());
            item.setContentPreview(reference.getSnippet());
            item.setScore(reference.getScore());
            result.add(item);
        }
        return result;
    }

    private List<String> extractTerms(String query) {
        Set<String> terms = new LinkedHashSet<>();
        String normalized = normalize(query);
        if (normalized.length() >= 2) {
            terms.add(normalized);
            if (normalized.length() <= 8) {
                for (int gram = 2; gram <= Math.min(4, normalized.length()); gram++) {
                    for (int i = 0; i <= normalized.length() - gram && terms.size() < 16; i++) {
                        terms.add(normalized.substring(i, i + gram));
                    }
                }
            }
        }
        return new ArrayList<>(terms);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{Punct}\\s]+", "").toLowerCase(Locale.ROOT);
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    /**
     * 判空工具方法
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
