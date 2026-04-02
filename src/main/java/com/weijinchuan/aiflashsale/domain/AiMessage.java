package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话消息实体
 */
@Data
@TableName("ai_message")
public class AiMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;
    private String role;
    private String messageType;
    private String content;
    private String intentJson;
    private LocalDateTime createTime;
}
