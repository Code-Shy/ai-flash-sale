package com.weijinchuan.aiflashsale.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weijinchuan.aiflashsale.common.constant.OrderStatusConstants;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.Cart;
import com.weijinchuan.aiflashsale.domain.CartItem;
import com.weijinchuan.aiflashsale.domain.OrderItem;
import com.weijinchuan.aiflashsale.domain.OrderOperateLog;
import com.weijinchuan.aiflashsale.domain.Orders;
import com.weijinchuan.aiflashsale.domain.Sku;
import com.weijinchuan.aiflashsale.event.OrderCompletedMessage;
import com.weijinchuan.aiflashsale.dto.order.SubmitOrderDTO;
import com.weijinchuan.aiflashsale.event.OrderCreatedMessage;
import com.weijinchuan.aiflashsale.event.OrderPaidMessage;
import com.weijinchuan.aiflashsale.mapper.CartItemMapper;
import com.weijinchuan.aiflashsale.mapper.CartMapper;
import com.weijinchuan.aiflashsale.mapper.InventoryMapper;
import com.weijinchuan.aiflashsale.mapper.OrderItemMapper;
import com.weijinchuan.aiflashsale.mapper.OrderOperateLogMapper;
import com.weijinchuan.aiflashsale.mapper.OrdersMapper;
import com.weijinchuan.aiflashsale.mapper.SkuMapper;
import com.weijinchuan.aiflashsale.service.OrderService;
import com.weijinchuan.aiflashsale.service.OutboxEventService;
import com.weijinchuan.aiflashsale.vo.OrderDetailVO;
import com.weijinchuan.aiflashsale.vo.OrderItemVO;
import com.weijinchuan.aiflashsale.vo.OrderListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String OPERATE_BY_SYSTEM = "SYSTEM";
    private static final String OPERATE_BY_USER = "USER";
    private static final String OPERATE_TYPE_CREATE = "CREATE";
    private static final String OPERATE_TYPE_CANCEL = "CANCEL";
    private static final String OPERATE_TYPE_PAY = "PAY";
    private static final String OPERATE_TYPE_COMPLETE = "COMPLETE";
    private static final String OPERATE_TYPE_EXPIRE = "EXPIRE";

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final InventoryMapper inventoryMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderOperateLogMapper orderOperateLogMapper;
    private final SkuMapper skuMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    private final OutboxEventService outboxEventService;

    @Value("${order.timeout.minutes:30}")
    private long orderTimeoutMinutes;

    /**
     * 提交订单
     *
     * 业务流程：
     * 1. 查询用户在指定门店下的有效购物车
     * 2. 取出已选中的购物车项
     * 3. 校验购物车是否为空
     * 4. 逐个锁定库存
     * 5. 创建订单主表
     * 6. 创建订单项
     * 7. 记录订单日志
     *
     * 注意：
     * 整个流程必须放在事务中，否则会出现库存已锁但订单没创建成功的问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(SubmitOrderDTO dto, String submitToken) {
        // ========= 1. 校验并消费幂等 token =========
        // 同一个 token 只能成功消费一次。
        // 如果 token 不存在，说明：
        // 1. 已经提交过
        // 2. token 失效
        String tokenKey = com.weijinchuan.aiflashsale.common.constant.RedisKeyConstants.ORDER_SUBMIT_TOKEN_PREFIX
                + dto.getUserId() + ":" + submitToken;

        String tokenValue = stringRedisTemplate.opsForValue().getAndDelete(tokenKey);
        if (!"1".equals(tokenValue)) {
            throw new BizException(5003, "请勿重复提交订单");
        }

        // 查询当前用户在当前门店下的有效购物车
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, dto.getUserId())
                        .eq(Cart::getStoreId, dto.getStoreId())
                        .eq(Cart::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (cart == null) {
            throw new BizException(4002, "购物车不存在");
        }

        // 查询已选中的购物车项
        List<CartItem> checkedItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getCartId, cart.getId())
                        .eq(CartItem::getChecked, 1)
        );
        if (checkedItems == null || checkedItems.isEmpty()) {
            throw new BizException(4003, "购物车没有可下单商品");
        }

        // 先计算总金额，再锁库存
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : checkedItems) {
            BigDecimal itemTotal = item.getPriceSnapshot()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // 锁定库存
            int updated = inventoryMapper.lockStock(
                    item.getStoreId(),
                    item.getSkuId(),
                    item.getQuantity()
            );
            if (updated <= 0) {
                throw new BizException(5002, "商品库存不足，SKU=" + item.getSkuId());
            }
        }

        // 生成订单号
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 创建订单主表
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setStoreId(dto.getStoreId());
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setOrderStatus(OrderStatusConstants.PENDING_PAYMENT);
        order.setRemark(dto.getRemark());
        order.setExpireTime(LocalDateTime.now().plusMinutes(orderTimeoutMinutes));
        ordersMapper.insert(order);

        // 创建订单项
        for (CartItem item : checkedItems) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku == null) {
                throw new BizException(404, "商品不存在，SKU=" + item.getSkuId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setStoreId(item.getStoreId());
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSkuName(sku.getSkuName());
            orderItem.setSkuImage(sku.getImageUrl());
            orderItem.setSalePrice(item.getPriceSnapshot());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalAmount(
                    item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            orderItemMapper.insert(orderItem);
        }

        logOrderStatusChange(order.getId(), orderNo, null, OrderStatusConstants.PENDING_PAYMENT,
                OPERATE_TYPE_CREATE, OPERATE_BY_USER, "创建订单");

        // ========= 2. 删除已下单的购物车项 =========
        for (CartItem item : checkedItems) {
            cartItemMapper.deleteById(item.getId());
        }

        // ========= 3. 写入 Outbox，事务提交后再发送 Kafka =========
        OrderCreatedMessage message = new OrderCreatedMessage();
        message.setOrderId(order.getId());
        message.setOrderNo(order.getOrderNo());
        message.setUserId(order.getUserId());
        message.setStoreId(order.getStoreId());
        message.setPayAmount(order.getPayAmount());
        message.setCreateTime(order.getCreateTime());

        outboxEventService.saveOrderCreatedEvent(message);

        return order.getId();
    }

    /**
     * 查询订单详情
     */
    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        validateOrderOwner(userId, order);

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getId)
        );

        List<OrderItemVO> itemVOList = new ArrayList<>();
        for (OrderItem item : orderItems) {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setSkuId(item.getSkuId());
            itemVO.setSkuName(item.getSkuName());
            itemVO.setSkuImage(item.getSkuImage());
            itemVO.setSalePrice(item.getSalePrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setTotalAmount(item.getTotalAmount());
            itemVOList.add(itemVO);
        }

        OrderDetailVO detailVO = new OrderDetailVO();
        detailVO.setOrderId(order.getId());
        detailVO.setOrderNo(order.getOrderNo());
        detailVO.setUserId(order.getUserId());
        detailVO.setStoreId(order.getStoreId());
        detailVO.setTotalAmount(order.getTotalAmount());
        detailVO.setPayAmount(order.getPayAmount());
        detailVO.setDeliveryFee(order.getDeliveryFee());
        detailVO.setOrderStatus(order.getOrderStatus());
        detailVO.setRemark(order.getRemark());
        detailVO.setExpireTime(order.getExpireTime());
        detailVO.setCreateTime(order.getCreateTime());
        detailVO.setItems(itemVOList);
        return detailVO;
    }

    /**
     * 取消订单
     *
     * 业务规则：
     * 1. 只有待支付订单可以取消
     * 2. 取消后需要回滚之前锁定的库存
     * 3. 更新订单状态
     * 4. 记录操作日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Orders order = getOrderForUpdate(orderId);
        validateOrderOwner(userId, order);

        if (order.getOrderStatus() != OrderStatusConstants.PENDING_PAYMENT) {
            throw new BizException(4004, "当前订单状态不允许取消");
        }

        closePendingOrder(order, OPERATE_TYPE_CANCEL, OPERATE_BY_USER, "取消订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        Orders order = getOrderForUpdate(orderId);
        validateOrderOwner(userId, order);

        if (order.getOrderStatus() != OrderStatusConstants.PENDING_PAYMENT) {
            throw new BizException(4005, "当前订单状态不允许支付");
        }

        if (order.getExpireTime() != null && !order.getExpireTime().isAfter(LocalDateTime.now())) {
            closePendingOrder(order, OPERATE_TYPE_EXPIRE, OPERATE_BY_SYSTEM, "订单支付时已超时，自动取消");
            throw new BizException(4006, "订单已超时，无法继续支付");
        }

        List<OrderItem> orderItems = getOrderItems(order.getId());
        for (OrderItem item : orderItems) {
            int updated = inventoryMapper.confirmLockedStock(
                    order.getStoreId(),
                    item.getSkuId(),
                    item.getQuantity()
            );
            if (updated <= 0) {
                throw new BizException(5006, "确认库存失败，SKU=" + item.getSkuId());
            }
        }

        order.setExpireTime(null);
        updateOrderStatus(order, OrderStatusConstants.PAID);
        logOrderStatusChange(order.getId(), order.getOrderNo(),
                OrderStatusConstants.PENDING_PAYMENT, OrderStatusConstants.PAID,
                OPERATE_TYPE_PAY, OPERATE_BY_USER, "支付订单");

        OrderPaidMessage message = new OrderPaidMessage();
        message.setOrderId(order.getId());
        message.setOrderNo(order.getOrderNo());
        message.setUserId(order.getUserId());
        message.setStoreId(order.getStoreId());
        message.setPayAmount(order.getPayAmount());
        message.setPaidTime(LocalDateTime.now());
        outboxEventService.saveOrderPaidEvent(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long userId, Long orderId) {
        Orders order = getOrderForUpdate(orderId);
        validateOrderOwner(userId, order);

        if (order.getOrderStatus() != OrderStatusConstants.PAID) {
            throw new BizException(4007, "当前订单状态不允许完成");
        }

        updateOrderStatus(order, OrderStatusConstants.COMPLETED);
        logOrderStatusChange(order.getId(), order.getOrderNo(),
                OrderStatusConstants.PAID, OrderStatusConstants.COMPLETED,
                OPERATE_TYPE_COMPLETE, OPERATE_BY_USER, "完成订单");

        OrderCompletedMessage message = new OrderCompletedMessage();
        message.setOrderId(order.getId());
        message.setOrderNo(order.getOrderNo());
        message.setUserId(order.getUserId());
        message.setStoreId(order.getStoreId());
        message.setPayAmount(order.getPayAmount());
        message.setCompletedTime(LocalDateTime.now());
        outboxEventService.saveOrderCompletedEvent(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireOrder(Long orderId) {
        Orders order = getOrderForUpdate(orderId);

        if (order.getOrderStatus() != OrderStatusConstants.PENDING_PAYMENT) {
            return;
        }

        if (order.getExpireTime() == null || order.getExpireTime().isAfter(LocalDateTime.now())) {
            return;
        }

        closePendingOrder(order, OPERATE_TYPE_EXPIRE, OPERATE_BY_SYSTEM, "订单超时自动取消");
    }

    /**
     * 查询用户订单列表
     */
    @Override
    public List<OrderListVO> listOrders(Long userId) {
        List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .orderByDesc(Orders::getId)
        );

        List<OrderListVO> result = new ArrayList<>();
        for (Orders order : orders) {
            OrderListVO vo = new OrderListVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setStoreId(order.getStoreId());
            vo.setPayAmount(order.getPayAmount());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setCreateTime(order.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    /**
     * 生成下单幂等 token
     *
     * 逻辑：
     * 1. 为指定用户生成一个唯一 token
     * 2. 写入 Redis
     * 3. 设置 10 分钟过期
     */
    @Override
    public String generateSubmitToken(Long userId) {
        String token = IdUtil.simpleUUID();
        String tokenKey = com.weijinchuan.aiflashsale.common.constant.RedisKeyConstants.ORDER_SUBMIT_TOKEN_PREFIX
                + userId + ":" + token;

        stringRedisTemplate.opsForValue().set(tokenKey, "1", 10, java.util.concurrent.TimeUnit.MINUTES);
        return token;
    }

    /**
     * 校验订单归属，避免越权查看或取消他人订单。
     */
    private void validateOrderOwner(Long userId, Orders order) {
        if (!userId.equals(order.getUserId())) {
            throw new BizException(403, "无权操作该订单");
        }
    }

    private Orders getOrderForUpdate(Long orderId) {
        Orders order = ordersMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        return order;
    }

    private List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
        );
    }

    private void rollbackLockedInventory(Orders order) {
        List<OrderItem> orderItems = getOrderItems(order.getId());
        for (OrderItem item : orderItems) {
            int updated = inventoryMapper.rollbackLockedStock(
                    order.getStoreId(),
                    item.getSkuId(),
                    item.getQuantity()
            );
            if (updated <= 0) {
                throw new BizException(5004, "库存回滚失败，SKU=" + item.getSkuId());
            }
        }
    }

    private void closePendingOrder(Orders order,
                                   String operateType,
                                   String operateBy,
                                   String remark) {
        rollbackLockedInventory(order);
        order.setExpireTime(null);
        updateOrderStatus(order, OrderStatusConstants.CANCELED);
        logOrderStatusChange(order.getId(), order.getOrderNo(),
                OrderStatusConstants.PENDING_PAYMENT, OrderStatusConstants.CANCELED,
                operateType, operateBy, remark);
    }

    private void updateOrderStatus(Orders order, Integer targetStatus) {
        order.setOrderStatus(targetStatus);
        ordersMapper.updateById(order);
    }

    private void logOrderStatusChange(Long orderId,
                                      String orderNo,
                                      Integer beforeStatus,
                                      Integer afterStatus,
                                      String operateType,
                                      String operateBy,
                                      String remark) {
        OrderOperateLog log = new OrderOperateLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setOperateType(operateType);
        log.setOperateBy(operateBy);
        log.setRemark(remark);
        orderOperateLogMapper.insert(log);
    }
}
