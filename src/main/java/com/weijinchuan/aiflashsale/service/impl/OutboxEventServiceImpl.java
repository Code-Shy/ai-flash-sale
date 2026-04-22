package com.weijinchuan.aiflashsale.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.common.constant.KafkaTopicConstants;
import com.weijinchuan.aiflashsale.common.constant.OutboxEventTypeConstants;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.OutboxEvent;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import com.weijinchuan.aiflashsale.event.OrderCompletedMessage;
import com.weijinchuan.aiflashsale.event.OrderPaidMessage;
import com.weijinchuan.aiflashsale.event.OrderTimeoutMessage;
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

    public static final int STATUS_PENDING = 0;

    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Long saveOrderCreatedEvent(OrderCreatedMessage message) {
        return saveEvent(
                OutboxEventTypeConstants.ORDER_CREATED,
                KafkaTopicConstants.ORDER_CREATED_TOPIC,
                String.valueOf(message.getOrderId()),
                message
        );
    }

    @Override
    public Long saveOrderPaidEvent(OrderPaidMessage message) {
        return saveEvent(
                OutboxEventTypeConstants.ORDER_PAID,
                KafkaTopicConstants.ORDER_PAID_TOPIC,
                String.valueOf(message.getOrderId()),
                message
        );
    }

    @Override
    public Long saveOrderCompletedEvent(OrderCompletedMessage message) {
        return saveEvent(
                OutboxEventTypeConstants.ORDER_COMPLETED,
                KafkaTopicConstants.ORDER_COMPLETED_TOPIC,
                String.valueOf(message.getOrderId()),
                message
        );
    }

    @Override
    public Long saveOrderTimeoutEvent(OrderTimeoutMessage message) {
        return saveEvent(
                OutboxEventTypeConstants.ORDER_TIMEOUT,
                KafkaTopicConstants.ORDER_TIMEOUT_TOPIC,
                String.valueOf(message.getOrderId()),
                message
        );
    }

    private Long saveEvent(String eventType, String topic, String eventKey, Object payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(eventType);
        outboxEvent.setTopic(topic);
        outboxEvent.setEventKey(eventKey);
        outboxEvent.setPayload(serialize(payload));
        outboxEvent.setStatus(STATUS_PENDING);
        outboxEvent.setRetryCount(0);
        outboxEvent.setNextRetryTime(LocalDateTime.now());
        outboxEventMapper.insert(outboxEvent);

        applicationEventPublisher.publishEvent(new OutboxEventCreated(outboxEvent.getId()));
        return outboxEvent.getId();
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BizException(5005, "序列化 Outbox 事件失败");
        }
    }
}
