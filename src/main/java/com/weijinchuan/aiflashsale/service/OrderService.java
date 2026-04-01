package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.dto.order.SubmitOrderDTO;
import com.weijinchuan.aiflashsale.vo.OrderDetailVO;
import com.weijinchuan.aiflashsale.vo.OrderListVO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 生成下单幂等 token
     *
     * @param userId 用户 ID
     * @return token
     */
    String generateSubmitToken(Long userId);

    /**
     * 提交订单
     *
     * @param dto 请求参数
     * @param submitToken 幂等 token
     * @return 订单 ID
     */
    Long submitOrder(SubmitOrderDTO dto, String submitToken);

    /**
     * 查询订单详情
     */
    OrderDetailVO getOrderDetail(Long userId, Long orderId);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * 支付订单
     */
    void payOrder(Long userId, Long orderId);

    /**
     * 完成订单
     */
    void completeOrder(Long userId, Long orderId);

    /**
     * 超时关闭订单
     */
    void expireOrder(Long orderId);

    /**
     * 查询用户订单列表
     */
    List<OrderListVO> listOrders(Long userId);
}
