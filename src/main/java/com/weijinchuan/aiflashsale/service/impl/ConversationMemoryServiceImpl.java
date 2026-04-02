package com.weijinchuan.aiflashsale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.AiMessage;
import com.weijinchuan.aiflashsale.domain.AiSession;
import com.weijinchuan.aiflashsale.mapper.AiMessageMapper;
import com.weijinchuan.aiflashsale.mapper.AiSessionMapper;
import com.weijinchuan.aiflashsale.service.ConversationMemoryService;
import com.weijinchuan.aiflashsale.vo.ai.ShoppingIntentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * AI 会话记忆服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private static final int STATUS_ACTIVE = 1;
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final ObjectMapper objectMapper;

    @Value("${ai-shopping.conversation.session-title-length:18}")
    private int sessionTitleLength;

    @Override
    public AiSession loadOrCreateSession(Long userId, Long storeId, Long sessionId, String firstQuery) {
        if (sessionId != null) {
            AiSession session = aiSessionMapper.selectById(sessionId);
            if (session == null
                    || !Objects.equals(session.getUserId(), userId)
                    || !Objects.equals(session.getStoreId(), storeId)
                    || !Objects.equals(session.getStatus(), STATUS_ACTIVE)) {
                throw new BizException(400, "AI 会话不存在或无权访问");
            }
            return session;
        }

        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setStoreId(storeId);
        session.setTitle(buildSessionTitle(firstQuery));
        session.setLastQuery(firstQuery);
        session.setStatus(STATUS_ACTIVE);
        aiSessionMapper.insert(session);
        return session;
    }

    @Override
    public List<AiMessage> listRecentMessages(Long sessionId, int limit) {
        if (sessionId == null || limit <= 0) {
            return List.of();
        }

        List<AiMessage> messages = aiMessageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, sessionId)
                        .orderByDesc(AiMessage::getCreateTime)
                        .last("LIMIT " + limit)
        );

        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        List<AiMessage> result = new ArrayList<>(messages);
        Collections.reverse(result);
        return result;
    }

    @Override
    public ShoppingIntentVO buildSessionIntent(AiSession session, List<AiMessage> recentMessages) {
        ShoppingIntentVO sessionIntent = parseIntent(session == null ? null : session.getLastIntentJson());
        if (isMeaningful(sessionIntent)) {
            return sessionIntent;
        }

        if (recentMessages == null || recentMessages.isEmpty()) {
            return new ShoppingIntentVO();
        }

        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            ShoppingIntentVO messageIntent = parseIntent(recentMessages.get(i).getIntentJson());
            if (isMeaningful(messageIntent)) {
                return messageIntent;
            }
        }
        return new ShoppingIntentVO();
    }

    @Override
    public void recordUserMessage(AiSession session, String messageType, String content, ShoppingIntentVO intent) {
        recordMessage(session, ROLE_USER, messageType, content, intent);
        session.setLastQuery(content);
        session.setLastIntentJson(writeIntent(intent));
        aiSessionMapper.updateById(session);
    }

    @Override
    public void recordAssistantMessage(AiSession session, String messageType, String content, ShoppingIntentVO intent) {
        recordMessage(session, ROLE_ASSISTANT, messageType, content, intent);
        session.setLastIntentJson(writeIntent(intent));
        aiSessionMapper.updateById(session);
    }

    @Override
    public String summarizeRecentConversation(List<AiMessage> recentMessages, int limit) {
        if (recentMessages == null || recentMessages.isEmpty() || limit <= 0) {
            return "";
        }

        int start = Math.max(0, recentMessages.size() - limit);
        List<String> lines = new ArrayList<>();
        for (int i = start; i < recentMessages.size(); i++) {
            AiMessage message = recentMessages.get(i);
            String role = ROLE_ASSISTANT.equalsIgnoreCase(message.getRole()) ? "助手" : "用户";
            lines.add(role + "：" + abbreviate(message.getContent(), 48));
        }
        return String.join("\n", lines);
    }

    private void recordMessage(AiSession session,
                               String role,
                               String messageType,
                               String content,
                               ShoppingIntentVO intent) {
        AiMessage message = new AiMessage();
        message.setSessionId(session.getId());
        message.setRole(role);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setIntentJson(writeIntent(intent));
        aiMessageMapper.insert(message);
    }

    private String writeIntent(ShoppingIntentVO intent) {
        if (intent == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(intent);
        } catch (Exception e) {
            log.warn("序列化 AI 意图失败", e);
            return null;
        }
    }

    private ShoppingIntentVO parseIntent(String json) {
        if (json == null || json.isBlank()) {
            return new ShoppingIntentVO();
        }
        try {
            return objectMapper.readValue(json, ShoppingIntentVO.class);
        } catch (Exception e) {
            log.warn("解析 AI 会话意图失败", e);
            return new ShoppingIntentVO();
        }
    }

    private String buildSessionTitle(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            return "AI 导购会话";
        }
        if (normalized.length() <= sessionTitleLength) {
            return normalized;
        }
        return normalized.substring(0, sessionTitleLength) + "...";
    }

    private String abbreviate(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        String normalized = content.trim().replace('\n', ' ');
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private boolean isMeaningful(ShoppingIntentVO intent) {
        if (intent == null) {
            return false;
        }
        return !isBlank(intent.getCategoryKeyword())
                || !isBlank(intent.getProductKeyword())
                || !isBlank(intent.getSceneKeyword())
                || !isBlank(intent.getTastePreference())
                || intent.getBudget() != null;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
