package com.weijinchuan.aiflashsale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weijinchuan.aiflashsale.domain.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单 Mapper
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 按订单 ID 加行锁读取，避免并发状态迁移冲突。
     */
    @Select("""
        SELECT *
        FROM orders
        WHERE id = #{orderId}
        FOR UPDATE
        """)
    Orders selectByIdForUpdate(@Param("orderId") Long orderId);

    /**
     * 查询超时未支付订单 ID。
     */
    @Select("""
        SELECT id
        FROM orders
        WHERE order_status = 10
          AND expire_time IS NOT NULL
          AND expire_time <= NOW()
        ORDER BY id ASC
        LIMIT #{limit}
        """)
    List<Long> selectExpiredPendingOrderIds(@Param("limit") int limit);

    /**
     * 查询仍在待支付且尚未过期的订单（用于崩溃恢复补发超时消息）。
     * 返回 Orders 对象以便获取 expireTime。
     */
    @Select("""
        SELECT id, expire_time
        FROM orders
        WHERE order_status = 10
          AND expire_time IS NOT NULL
          AND expire_time > NOW()
          AND id > #{minId}
        ORDER BY id ASC
        LIMIT #{limit}
        """)
    List<Orders> selectPendingNotExpiredOrders(@Param("minId") long minId, @Param("limit") int limit);
}
