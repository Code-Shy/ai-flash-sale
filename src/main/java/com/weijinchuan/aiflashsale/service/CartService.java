package com.weijinchuan.aiflashsale.service;

import com.weijinchuan.aiflashsale.dto.cart.AddCartItemDTO;
import com.weijinchuan.aiflashsale.dto.cart.UpdateCartItemDTO;
import com.weijinchuan.aiflashsale.vo.CartVO;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 加入购物车
     *
     * @param dto 请求参数
     */
    void addItem(AddCartItemDTO dto);

    /**
     * 查询用户在某个门店下的购物车
     *
     * @param userId 用户 ID
     * @param storeId 门店 ID
     * @return 购物车详情
     */
    CartVO getCart(Long userId, Long storeId);

    /**
     * 修改购物车项数量
     *
     * @param itemId 购物车项 ID
     * @param dto 请求参数
     */
    void updateItemQuantity(Long itemId, UpdateCartItemDTO dto);

    /**
     * 删除购物车项
     *
     * @param itemId 购物车项 ID
     */
    void deleteItem(Long itemId);
}