package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.dto.order.SubmitOrderDTO;
import com.weijinchuan.aiflashsale.service.OrderService;
import com.weijinchuan.aiflashsale.vo.OrderDetailVO;
import com.weijinchuan.aiflashsale.vo.OrderListVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口控制器
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取下单幂等 token
     */
    @GetMapping("/token")
    @Operation(summary = "获取下单幂等 token")
    public Result<String> generateSubmitToken(@RequestParam Long userId) {
        return Result.success(orderService.generateSubmitToken(userId));
    }

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    @Operation(summary = "提交订单")
    public Result<Long> submitOrder(@Valid @RequestBody SubmitOrderDTO dto,
                                    @RequestHeader("Idempotency-Token") String submitToken) {
        return Result.success(orderService.submitOrder(dto, submitToken));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情")
    public Result<OrderDetailVO> getOrderDetail(@RequestParam Long userId,
                                                @PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(userId, orderId));
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(@RequestParam Long userId,
                                    @PathVariable Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return Result.success();
    }

    /**
     * 模拟支付订单，仅开发测试使用
     */
    @PostMapping("/{orderId}/pay")
    @Operation(summary = "模拟支付订单，仅开发测试使用")
    public Result<Void> payOrder(@RequestParam Long userId,
                                 @PathVariable Long orderId) {
        orderService.payOrder(userId, orderId);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PostMapping("/{orderId}/complete")
    @Operation(summary = "完成订单")
    public Result<Void> completeOrder(@RequestParam Long userId,
                                      @PathVariable Long orderId) {
        orderService.completeOrder(userId, orderId);
        return Result.success();
    }

    /**
     * 查询用户订单列表
     */
    @GetMapping
    @Operation(summary = "查询用户订单列表")
    public Result<List<OrderListVO>> listOrders(@RequestParam Long userId) {
        return Result.success(orderService.listOrders(userId));
    }
}
