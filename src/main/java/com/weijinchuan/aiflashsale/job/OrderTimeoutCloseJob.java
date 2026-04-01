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
 * 订单超时关闭任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutCloseJob {

    private final OrdersMapper ordersMapper;
    private final OrderService orderService;

    @Value("${order.timeout.close.batch-size:20}")
    private int batchSize;

    /**
     * 定时扫描超时未支付订单并释放库存。
     */
    @Scheduled(fixedDelayString = "${order.timeout.close.fixed-delay-ms:10000}")
    public void closeExpiredOrders() {
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
