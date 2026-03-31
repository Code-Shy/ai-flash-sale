package com.weijinchuan.aiflashsale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weijinchuan.aiflashsale.domain.Inventory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 库存 Mapper
 */
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 锁定库存
     *
     * 逻辑：
     * 1. available_stock 减少
     * 2. locked_stock 增加
     * 3. 只有可用库存足够时才更新成功
     *
     * @return 影响行数，1 表示成功，0 表示库存不足
     */
    @Update("""
        UPDATE inventory
        SET available_stock = available_stock - #{quantity},
            locked_stock = locked_stock + #{quantity},
            version = version + 1
        WHERE store_id = #{storeId}
          AND sku_id = #{skuId}
          AND available_stock >= #{quantity}
        """)
    int lockStock(@Param("storeId") Long storeId,
                  @Param("skuId") Long skuId,
                  @Param("quantity") Integer quantity);

    /**
     * 取消订单时回滚库存
     *
     * 逻辑：
     * 1. available_stock 加回
     * 2. locked_stock 扣减
     */
    @Update("""
        UPDATE inventory
        SET available_stock = available_stock + #{quantity},
            locked_stock = locked_stock - #{quantity},
            version = version + 1
        WHERE store_id = #{storeId}
          AND sku_id = #{skuId}
          AND locked_stock >= #{quantity}
        """)
    int rollbackLockedStock(@Param("storeId") Long storeId,
                            @Param("skuId") Long skuId,
                            @Param("quantity") Integer quantity);
}