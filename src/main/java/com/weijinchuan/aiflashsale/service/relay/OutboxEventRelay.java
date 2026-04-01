package com.weijinchuan.aiflashsale.service.relay;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weijinchuan.aiflashsale.domain.OutboxEvent;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import com.weijinchuan.aiflashsale.event.OutboxEventCreated;
import com.weijinchuan.aiflashsale.event.producer.OrderEventProducer;
import com.weijinchuan.aiflashsale.mapper.OutboxEventMapper;
import com.weijinchuan.aiflashsale.service.impl.OutboxEventServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外盒事件转发器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_FAILED = 2;
    private static final int STATUS_DEAD = 3;

    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    private final OrderEventProducer orderEventProducer;

    @Value("${outbox.retry.max-attempts:8}")
    private int maxAttempts;

    @Value("${outbox.retry.backoff-seconds:30}")
    private int backoffSeconds;

    @Value("${outbox.retry.batch-size:20}")
    private int batchSize;

    /**
     * 事务提交后立即尝试发送，避免回滚但消息已发。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventCreated event) {
        publishById(event.outboxEventId());
    }

    /**
     * 定时扫描失败或待发送事件，做重试补偿。
     */
    @Scheduled(fixedDelayString = "${outbox.retry.fixed-delay-ms:10000}")
    public void retryPendingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxEvent> events = outboxEventMapper.selectList(
                new LambdaQueryWrapper<OutboxEvent>()
                        .in(OutboxEvent::getStatus, STATUS_PENDING, STATUS_FAILED)
                        .le(OutboxEvent::getNextRetryTime, now)
                        .orderByAsc(OutboxEvent::getId)
                        .last("LIMIT " + batchSize)
        );

        for (OutboxEvent event : events) {
            publishById(event.getId());
        }
    }

    public void publishById(Long outboxEventId) {
        OutboxEvent outboxEvent = outboxEventMapper.selectById(outboxEventId);
        if (outboxEvent == null) {
            return;
        }

        if (STATUS_PUBLISHED == outboxEvent.getStatus()) {
            return;
        }

        if (outboxEvent.getRetryCount() != null && outboxEvent.getRetryCount() >= maxAttempts) {
            markDead(outboxEvent);
            return;
        }

        try {
            if (OutboxEventServiceImpl.EVENT_TYPE_ORDER_CREATED.equals(outboxEvent.getEventType())) {
                OrderCreatedMessage message = objectMapper.readValue(outboxEvent.getPayload(), OrderCreatedMessage.class);
                orderEventProducer.sendOrderCreatedMessage(message);
            } else {
                throw new IllegalStateException("不支持的外盒事件类型: " + outboxEvent.getEventType());
            }

            markPublished(outboxEvent);
        } catch (Exception e) {
            markFailed(outboxEvent, e);
        }
    }

    private void markPublished(OutboxEvent outboxEvent) {
        OutboxEvent update = new OutboxEvent();
        update.setId(outboxEvent.getId());
        update.setStatus(STATUS_PUBLISHED);
        update.setPublishedTime(LocalDateTime.now());
        update.setErrorMessage(null);
        outboxEventMapper.updateById(update);
    }

    private void markFailed(OutboxEvent outboxEvent, Exception e) {
        int currentRetryCount = outboxEvent.getRetryCount() == null ? 0 : outboxEvent.getRetryCount();

        OutboxEvent update = new OutboxEvent();
        update.setId(outboxEvent.getId());
        update.setStatus(STATUS_FAILED);
        update.setRetryCount(currentRetryCount + 1);
        update.setNextRetryTime(LocalDateTime.now().plusSeconds(backoffSeconds));
        update.setErrorMessage(trimErrorMessage(e));
        outboxEventMapper.updateById(update);

        log.warn("外盒事件发送失败，id={}, retryCount={}, reason={}",
                outboxEvent.getId(), currentRetryCount + 1, trimErrorMessage(e));
    }

    private void markDead(OutboxEvent outboxEvent) {
        OutboxEvent update = new OutboxEvent();
        update.setId(outboxEvent.getId());
        update.setStatus(STATUS_DEAD);
        update.setNextRetryTime(LocalDateTime.now().plusYears(100));
        update.setErrorMessage("超过最大重试次数，不再继续发送");
        outboxEventMapper.updateById(update);

        log.error("外盒事件进入终态，id={}, retryCount={}",
                outboxEvent.getId(), outboxEvent.getRetryCount());
    }

    private String trimErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }
        if (message.length() <= 255) {
            return message;
        }
        return message.substring(0, 255);
    }
}
