package com.weijinchuan.aiflashsale.vo.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 导购返回对象
 */
@Data
public class ShoppingRecommendVO {

    /**
     * 会话 ID
     */
    private Long sessionId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 原始查询
     */
    private String query;

    /**
     * 解析后的意图
     */
    private ShoppingIntentVO intent;

    /**
     * 导购总结
     */
    private String summary;

    /**
     * 记住的用户偏好
     */
    private List<String> rememberedPreferences;

    /**
     * 是否建议继续追问
     */
    private Boolean needClarification;

    /**
     * 追问内容
     */
    private String followUpQuestion;

    /**
     * 推荐结果
     */
    private List<ShoppingRecommendItemVO> recommendations;
}
