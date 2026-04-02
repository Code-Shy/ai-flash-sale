package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话实体
 */
@Data
@TableName("ai_session")
public class AiSession {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long storeId;
    private String title;
    private String lastQuery;
    private String lastIntentJson;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
