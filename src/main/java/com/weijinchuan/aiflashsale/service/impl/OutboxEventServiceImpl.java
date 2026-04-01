package com.weijinchuan.aiflashsale.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.OutboxEvent;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import com.weijinchuan.aiflashsale.event.OutboxEventCreated;
import com.weijinchuan.aiflashsale.mapper.OutboxEventMapper;
import com.weijinchuan.aiflashsale.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 外盒事件服务实现
 */
@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    public static final String EVENT_TYPE_ORDER_CREATED = "ORDER_CREATED";
    public static final int STATUS_PENDING = 0;

    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Long saveOrderCreatedEvent(OrderCreatedMessage message) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(EVENT_TYPE_ORDER_CREATED);
        outboxEvent.setTopic(KafkaTopicConstants.ORDER_CREATED_TOPIC);
        outboxEvent.setEventKey(String.valueOf(message.getOrderId()));
        outboxEvent.setPayload(serialize(message));
        outboxEvent.setStatus(STATUS_PENDING);
        outboxEvent.setRetryCount(0);
        outboxEvent.setNextRetryTime(LocalDateTime.now());
        outboxEventMapper.insert(outboxEvent);

        applicationEventPublisher.publishEvent(new OutboxEventCreated(outboxEvent.getId()));
        return outboxEvent.getId();
    }

    private String serialize(OrderCreatedMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new BizException(5005, "序列化订单创建事件失败");
        }
    }
}
