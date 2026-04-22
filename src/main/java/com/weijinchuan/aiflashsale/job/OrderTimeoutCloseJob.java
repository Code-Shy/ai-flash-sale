package com.weijinchuan.aiflashsale.job;

import com.weijinchuan.aiflashsale.mapper.OrdersMapper;
import com.weijinchuan.aiflashsale.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单超时关闭任务（兜底）
 *
 * 主流程已改为 Kafka 延时队列驱动，此 Job 作为兜底保障：
 * 处理极少数因消息丢失或消费失败而漏掉的超时订单。
 * 通过 order.timeout.close.enabled=false 可禁用（迁移稳定后建议保留作兜底）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutCloseJob {

    private final OrdersMapper ordersMapper;
    private final OrderService orderService;

    @Value("${order.timeout.close.batch-size:20}")
    private int batchSize;

    @Value("${order.timeout.close.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelayString = "${order.timeout.close.fixed-delay-ms:10000}")
    public void closeExpiredOrders() {
        if (!enabled) {
            return;
        }
        List<Long> orderIds = ordersMapper.selectExpiredPendingOrderIds(batchSize);
        for (Long orderId : orderIds) {
            try {
                orderService.expireOrder(orderId);
            } catch (Exception e) {
                log.warn("超时关闭订单失败，orderId={}, reason={}", orderId, e.getMessage());
            }
        }
    }
}
