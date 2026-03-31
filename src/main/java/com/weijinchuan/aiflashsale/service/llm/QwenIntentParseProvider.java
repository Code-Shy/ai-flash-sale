package com.weijinchuan.aiflashsale.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 通义千问意图解析 Provider
 *
 * 说明：
 * 1. 通过阿里云百炼兼容接口调用通义
 * 2. 只负责“自然语言 -> 结构化意图”
 * 3. 如果 API Key 未配置或调用失败，则回退到 Mock 解析器
 */
@Component
@Primary
@RequiredArgsConstructor
public class QwenIntentParseProvider implements IntentParseProvider {

    /**
     * JSON 处理器
     */
    private final ObjectMapper objectMapper;

    /**
     * 本地规则兜底解析器
     */
    private final MockIntentParseProvider mockIntentParseProvider;

    /**
     * 通义兼容接口地址
     */
    @Value("${qwen.base-url}")
    private String baseUrl;

    /**
     * API Key
     */
    @Value("${qwen.api-key:}")
    private String apiKey;

    /**
     * 模型名
     */
    @Value("${qwen.model:qwen-plus}")
    private String model;

    @Override
    public ShoppingIntentVO parseShoppingIntent(String query) {
        // 没配置 key，直接走本地规则兜底
        if (apiKey == null || apiKey.isBlank()) {
            return mockIntentParseProvider.parseShoppingIntent(query);
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .build();

            // 让模型严格输出 JSON
            String systemPrompt = """
                    你是一个电商导购意图解析器。
                    你的任务是把用户的购物需求解析成 JSON。

                    只允许输出 JSON，不要输出解释，不要输出 markdown 代码块。
                    JSON 字段固定为：
                    {
                      "rawQuery": "原始输入",
                      "categoryKeyword": "品类关键词，没有则为null",
                      "productKeyword": "商品关键词，没有则为null",
                      "sceneKeyword": "场景关键词，没有则为null",
                      "tastePreference": "口味偏好，没有则为null",
                      "budget": 数字预算，没有则为null
                    }
                    """;

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", query)
                    ),
                    "temperature", 0.1
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

            String pureJson = extractJson(content);
            ShoppingIntentVO intent = objectMapper.readValue(pureJson, ShoppingIntentVO.class);

            if (intent.getRawQuery() == null || intent.getRawQuery().isBlank()) {
                intent.setRawQuery(query);
            }

            return intent;
        } catch (Exception e) {
            // 出现异常时回退到 Mock 实现，保证主链路可用
            return mockIntentParseProvider.parseShoppingIntent(query);
        }
    }

    /**
     * 提取模型返回中的 JSON 文本
     *
     * 模型有时会返回 ```json ... ```，这里做清洗。
     */
    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            return "{}";
        }

        String trimmed = content.trim();

        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```json", "");
            trimmed = trimmed.replaceFirst("^```", "");
            trimmed = trimmed.replaceFirst("```$", "");
            trimmed = trimmed.trim();
        }

        int start = trimmed.indexOf("{");
        int end = trimmed.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        return trimmed;
    }
}