package com.weijinchuan.aiflashsale.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.service.rag.RetrievedKnowledge;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingRecommendItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 通义版导购生成器
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class QwenShoppingGenerationProvider implements ShoppingGenerationProvider {

    private final ObjectMapper objectMapper;
    private final MockShoppingGenerationProvider mockShoppingGenerationProvider;

    @Value("${qwen.base-url}")
    private String baseUrl;

    @Value("${qwen.api-key:}")
    private String apiKey;

    @Value("${qwen.model:qwen-plus}")
    private String model;

    @Override
    public String generateRecommendationReason(String query,
                                               ShoppingIntentVO intent,
                                               StoreSkuVO product,
                                               List<RetrievedKnowledge> references) {
        return generateText(
                """
                        你是即时零售导购助手。
                        请根据用户需求、商品信息和检索到的知识片段，生成一句到两句中文推荐理由。
                        要求：
                        1. 不要编造库存、配送时效、折扣和功效。
                        2. 不要使用 markdown。
                        3. 语气自然，尽量像导购，而不是写产品说明书。
                        """,
                """
                        用户需求：%s
                        结构化意图：%s
                        商品信息：%s
                        参考知识：
                        %s
                        """.formatted(query, formatIntent(intent), formatProduct(product), formatReferences(references)),
                () -> mockShoppingGenerationProvider.generateRecommendationReason(query, intent, product, references)
        );
    }

    @Override
    public String generateRecommendationSummary(String query,
                                                ShoppingIntentVO intent,
                                                List<ShoppingRecommendItemVO> recommendations) {
        return generateRecommendationSummary(query, intent, recommendations, "", List.of());
    }

    @Override
    public String generateRecommendationSummary(String query,
                                                ShoppingIntentVO intent,
                                                List<ShoppingRecommendItemVO> recommendations,
                                                String conversationContext,
                                                List<String> preferenceHints) {
        return generateText(
                """
                        你是即时零售导购助手。
                        请根据推荐商品列表写一段不超过两句的中文总结。
                        要求：
                        1. 说清楚推荐方向。
                        2. 不要编造价格优势和库存。
                        3. 不要使用 markdown。
                        """,
                """
                        用户需求：%s
                        结构化意图：%s
                        最近对话：
                        %s
                        已记住偏好：%s
                        推荐商品：
                        %s
                        """.formatted(
                        query,
                        formatIntent(intent),
                        safeText(conversationContext),
                        formatPreferenceHints(preferenceHints),
                        formatRecommendationList(recommendations)
                ),
                () -> mockShoppingGenerationProvider.generateRecommendationSummary(
                        query,
                        intent,
                        recommendations,
                        conversationContext,
                        preferenceHints
                )
        );
    }

    @Override
    public String generateAnswer(String query,
                                 ShoppingIntentVO intent,
                                 List<ShoppingRecommendItemVO> recommendations,
                                 List<RetrievedKnowledge> references) {
        return generateAnswer(query, intent, recommendations, references, "", List.of());
    }

    @Override
    public String generateAnswer(String query,
                                 ShoppingIntentVO intent,
                                 List<ShoppingRecommendItemVO> recommendations,
                                 List<RetrievedKnowledge> references,
                                 String conversationContext,
                                 List<String> preferenceHints) {
        return generateText(
                """
                        你是即时零售导购问答助手。
                        请基于检索到的知识片段和当前门店候选商品回答问题。
                        要求：
                        1. 回答控制在 3 到 5 句。
                        2. 只使用提供的事实，不要编造。
                        3. 如果知识不足，要明确说明信息有限。
                        4. 不要使用 markdown。
                        """,
                """
                        用户问题：%s
                        结构化意图：%s
                        最近对话：
                        %s
                        已记住偏好：%s
                        候选商品：
                        %s
                        参考知识：
                        %s
                        """.formatted(
                        query,
                        formatIntent(intent),
                        safeText(conversationContext),
                        formatPreferenceHints(preferenceHints),
                        formatRecommendationList(recommendations),
                        formatReferences(references)
                ),
                () -> mockShoppingGenerationProvider.generateAnswer(
                        query,
                        intent,
                        recommendations,
                        references,
                        conversationContext,
                        preferenceHints
                )
        );
    }

    private String generateText(String systemPrompt, String userPrompt, Supplier<String> fallback) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback.get();
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.3
            );

            String responseText = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseText);
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            String cleaned = cleanup(content);
            if (cleaned.isBlank()) {
                return fallback.get();
            }
            return cleaned;
        } catch (Exception e) {
            log.warn("调用通义生成导购内容失败，已回退到本地模板");
            return fallback.get();
        }
    }

    private String cleanup(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*", "");
            trimmed = trimmed.replaceFirst("```$", "");
            trimmed = trimmed.trim();
        }
        return trimmed.replace('\n', ' ').trim();
    }

    private String formatIntent(ShoppingIntentVO intent) {
        return "品类=" + safeText(intent.getCategoryKeyword())
                + "，商品=" + safeText(intent.getProductKeyword())
                + "，场景=" + safeText(intent.getSceneKeyword())
                + "，口味=" + safeText(intent.getTastePreference())
                + "，预算=" + (intent.getBudget() == null ? "未指定" : intent.getBudget());
    }

    private String formatProduct(StoreSkuVO product) {
        return "skuId=" + product.getSkuId()
                + "，商品名=" + safeText(product.getSkuName())
                + "，规格=" + safeText(product.getSpecs())
                + "，售价=" + (product.getSalePrice() == null ? "未知" : product.getSalePrice())
                + "，可用库存=" + (product.getAvailableStock() == null ? "未知" : product.getAvailableStock());
    }

    private String formatRecommendationList(List<ShoppingRecommendItemVO> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "暂无候选商品";
        }
        return recommendations.stream()
                .map(item -> item.getSkuName() + "（" + safeText(item.getReason()) + "）")
                .collect(Collectors.joining("\n"));
    }

    private String formatReferences(List<RetrievedKnowledge> references) {
        if (references == null || references.isEmpty()) {
            return "暂无知识片段";
        }
        return references.stream()
                .map(reference -> reference.getDocument().getTitle() + "：" + reference.getSnippet())
                .collect(Collectors.joining("\n"));
    }

    private String formatPreferenceHints(List<String> preferenceHints) {
        if (preferenceHints == null || preferenceHints.isEmpty()) {
            return "暂无稳定偏好";
        }
        return String.join("；", preferenceHints);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "未提及" : value;
    }
}
