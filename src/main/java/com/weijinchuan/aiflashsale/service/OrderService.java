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
     * 提交订单
     *
     * @param dto 请求参数
     * @return 订单 ID
     */
    Long submitOrder(SubmitOrderDTO dto);

    /**
     * 查询订单详情
     */
    OrderDetailVO getOrderDetail(Long orderId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId);

    /**
     * 查询用户订单列表
     */
    List<OrderListVO> listOrders(Long userId);
}