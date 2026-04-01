package com.weijinchuan.aiflashsale.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事务消息外盒事件
 */
@Data
@TableName("outbox_event")
public class OutboxEvent {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 事件类型，例如 ORDER_CREATED
     */
    private String eventType;

    /**
     * Kafka topic
     */
    private String topic;

    /**
     * Kafka key
     */
    private String eventKey;

    /**
     * JSON 序列化后的消息体
     */
    private String payload;

    /**
     * 状态：0-待发送 1-已发送 2-发送失败 3-终态失败
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 最近一次错误信息
     */
    private String errorMessage;

    /**
     * 成功发送时间
     */
    private LocalDateTime publishedTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
