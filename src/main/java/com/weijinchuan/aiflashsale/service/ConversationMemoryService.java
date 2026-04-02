package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.domain.AiMessage;
import com.weijinchuan.aiflashsale.domain.AiSession;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;

import java.util.List;

/**
 * AI 会话记忆服务
 */
public interface ConversationMemoryService {

    /**
     * 获取或创建会话。
     */
    AiSession loadOrCreateSession(Long userId, Long storeId, Long sessionId, String firstQuery);

    /**
     * 查询最近会话消息。
     */
    List<AiMessage> listRecentMessages(Long sessionId, int limit);

    /**
     * 从历史上下文中恢复意图。
     */
    ShoppingIntentVO buildSessionIntent(AiSession session, List<AiMessage> recentMessages);

    /**
     * 保存用户消息，并刷新会话状态。
     */
    void recordUserMessage(AiSession session, String messageType, String content, ShoppingIntentVO intent);

    /**
     * 保存助手消息。
     */
    void recordAssistantMessage(AiSession session, String messageType, String content, ShoppingIntentVO intent);

    /**
     * 为大模型生成最近几轮对话摘要。
     */
    String summarizeRecentConversation(List<AiMessage> recentMessages, int limit);
}
