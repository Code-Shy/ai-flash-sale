package com.weijinchuan.aiflashsale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weijinchuan.aiflashsale.domain.PaymentOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 支付单 Mapper
 */
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    /**
     * 按商户单号加锁读取支付单。
     */
    @Select("""
        SELECT *
        FROM payment_order
        WHERE out_trade_no = #{outTradeNo}
        LIMIT 1
        FOR UPDATE
        """)
    PaymentOrder selectByOutTradeNoForUpdate(@Param("outTradeNo") String outTradeNo);
}
