package com.weijinchuan.aiflashsale.job;

import com.weijinchuan.aiflashsale.domain.Orders;
import com.weijinchuan.aiflashsale.event.OrderTimeoutMessage;
import com.weijinchuan.aiflashsale.mapper.OrdersMapper;
import com.weijinchuan.aiflashsale.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单超时消息崩溃恢复 Job
 *
 * 问题背景：
 *   服务崩溃重启后，部分订单的超时延时消息可能已丢失（Outbox 未发出或 Kafka 未消费）。
 *   若不补发，这些订单将永远停留在 PENDING_PAYMENT 状态，库存无法释放。
 *
 * 恢复策略：
 *   应用启动完成后，扫描 DB 中仍处于 PENDING_PAYMENT 且 expireTime > now 的订单，
 *   通过 Outbox 重新发送超时消息。
 *   - Consumer 端有 Redis 幂等去重，重复消息不会重复关单
 *   - 已过期的订单（expireTime <= now）由 OrderTimeoutCloseJob 兜底处理
 *
 * 分页游标扫描：避免一次性加载大量数据 OOM。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutRecoveryJob {

    private final OrdersMapper ordersMapper;
    private final OutboxEventService outboxEventService;

    @Value("${order.timeout.recovery.batch-size:100}")
    private int batchSize;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverOnStartup() {
        log.info("开始扫描待恢复的超时订单消息...");
        long minId = 0;
        int totalRecovered = 0;

        while (true) {
            List<Orders> batch = ordersMapper.selectPendingNotExpiredOrders(minId, batchSize);
            if (batch.isEmpty()) {
                break;
            }

            for (Orders order : batch) {
                try {
                    resendTimeoutMessage(order);
                    totalRecovered++;
                } catch (Exception e) {
                    log.warn("补发超时消息失败，orderId={}, reason={}", order.getId(), e.getMessage());
                }
            }

            minId = batch.get(batch.size() - 1).getId();
            if (batch.size() < batchSize) {
                break;
            }
        }

        log.info("超时订单消息恢复完成，共补发 {} 条", totalRecovered);
    }

    /**
     * 通过 Outbox 重新发送超时消息（事务保证原子性）。
     * Consumer 端 Redis 幂等 key 若已存在则自动跳过，不会重复关单。
     */
    @Transactional(rollbackFor = Exception.class)
    public void resendTimeoutMessage(Orders order) {
        OrderTimeoutMessage msg = new OrderTimeoutMessage();
        msg.setOrderId(order.getId());
        msg.setExecuteAt(order.getExpireTime());
        outboxEventService.saveOrderTimeoutEvent(msg);
    }
}
