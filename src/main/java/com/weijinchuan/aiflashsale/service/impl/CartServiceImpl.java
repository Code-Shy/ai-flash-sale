package com.weijinchuan.aiflashsale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weijinchuan.aiflashsale.common.exception.BizException;
import com.weijinchuan.aiflashsale.domain.Cart;
import com.weijinchuan.aiflashsale.domain.CartItem;
import com.weijinchuan.aiflashsale.domain.StoreSku;
import com.weijinchuan.aiflashsale.domain.Sku;
import com.weijinchuan.aiflashsale.dto.cart.AddCartItemDTO;
import com.weijinchuan.aiflashsale.dto.cart.UpdateCartItemDTO;
import com.weijinchuan.aiflashsale.mapper.CartItemMapper;
import com.weijinchuan.aiflashsale.mapper.CartMapper;
import com.weijinchuan.aiflashsale.mapper.SkuMapper;
import com.weijinchuan.aiflashsale.mapper.StoreSkuMapper;
import com.weijinchuan.aiflashsale.service.CartService;
import com.weijinchuan.aiflashsale.vo.CartItemVO;
import com.weijinchuan.aiflashsale.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final SkuMapper skuMapper;
    private final StoreSkuMapper storeSkuMapper;

    /**
     * 加入购物车
     *
     * 业务规则：
     * 1. 先校验商品是否存在
     * 2. 再校验该门店下是否可售
     * 3. 查找当前用户在当前门店下是否已有购物车
     * 4. 如果已有相同 SKU，则累加数量
     * 5. 如果没有，则新增购物车项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(AddCartItemDTO dto) {
        // 校验 SKU 是否存在
        Sku sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null || sku.getStatus() == 0) {
            throw new BizException(404, "商品不存在或已下架");
        }

        // 校验门店商品是否存在且可售
        StoreSku storeSku = storeSkuMapper.selectOne(
                new LambdaQueryWrapper<StoreSku>()
                        .eq(StoreSku::getStoreId, dto.getStoreId())
                        .eq(StoreSku::getSkuId, dto.getSkuId())
                        .eq(StoreSku::getSaleStatus, 1)
        );
        if (storeSku == null) {
            throw new BizException(4001, "该门店暂无此商品");
        }

        // 查询当前用户在该门店下是否已有有效购物车
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, dto.getUserId())
                        .eq(Cart::getStoreId, dto.getStoreId())
                        .eq(Cart::getStatus, 1)
                        .last("LIMIT 1")
        );

        // 如果没有购物车，则新建购物车
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(dto.getUserId());
            cart.setStoreId(dto.getStoreId());
            cart.setStatus(1);
            cartMapper.insert(cart);
        }

        // 查询当前购物车中是否已存在该 SKU
        CartItem existingItem = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getCartId, cart.getId())
                        .eq(CartItem::getSkuId, dto.getSkuId())
                        .last("LIMIT 1")
        );

        // 已存在则累加数量
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
            cartItemMapper.updateById(existingItem);
            return;
        }

        // 不存在则新增购物车项
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setUserId(dto.getUserId());
        cartItem.setStoreId(dto.getStoreId());
        cartItem.setSkuId(dto.getSkuId());
        cartItem.setQuantity(dto.getQuantity());
        cartItem.setChecked(1);
        cartItem.setPriceSnapshot(storeSku.getSalePrice());
        cartItemMapper.insert(cartItem);
    }

    /**
     * 查询购物车
     */
    @Override
    public CartVO getCart(Long userId, Long storeId) {
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getStoreId, storeId)
                        .eq(Cart::getStatus, 1)
                        .last("LIMIT 1")
        );

        // 没有购物车时，返回空购物车对象，而不是直接报错
        if (cart == null) {
            CartVO emptyCart = new CartVO();
            emptyCart.setUserId(userId);
            emptyCart.setStoreId(storeId);
            emptyCart.setItems(new ArrayList<>());
            emptyCart.setTotalAmount(BigDecimal.ZERO);
            return emptyCart;
        }

        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getCartId, cart.getId())
                        .orderByDesc(CartItem::getId)
        );

        List<CartItemVO> itemVOList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku == null) {
                continue;
            }

            CartItemVO itemVO = new CartItemVO();
            itemVO.setItemId(item.getId());
            itemVO.setSkuId(item.getSkuId());
            itemVO.setSkuName(sku.getSkuName());
            itemVO.setSpecs(sku.getSpecs());
            itemVO.setImageUrl(sku.getImageUrl());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setChecked(item.getChecked());
            itemVO.setPriceSnapshot(item.getPriceSnapshot());

            BigDecimal itemTotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemVO.setTotalAmount(itemTotal);

            itemVOList.add(itemVO);

            // 这里只统计选中商品金额
            if (item.getChecked() != null && item.getChecked() == 1) {
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        CartVO cartVO = new CartVO();
        cartVO.setCartId(cart.getId());
        cartVO.setUserId(cart.getUserId());
        cartVO.setStoreId(cart.getStoreId());
        cartVO.setItems(itemVOList);
        cartVO.setTotalAmount(totalAmount);
        return cartVO;
    }

    /**
     * 修改购物车项数量
     */
    @Override
    public void updateItemQuantity(Long userId, Long itemId, UpdateCartItemDTO dto) {
        CartItem cartItem = cartItemMapper.selectById(itemId);
        if (cartItem == null) {
            throw new BizException(404, "购物车项不存在");
        }
        validateCartItemOwner(userId, cartItem);

        cartItem.setQuantity(dto.getQuantity());
        cartItemMapper.updateById(cartItem);
    }

    /**
     * 删除购物车项
     */
    @Override
    public void deleteItem(Long userId, Long itemId) {
        CartItem cartItem = cartItemMapper.selectById(itemId);
        if (cartItem == null) {
            throw new BizException(404, "购物车项不存在");
        }
        validateCartItemOwner(userId, cartItem);
        cartItemMapper.deleteById(itemId);
    }

    /**
     * 校验购物车项归属，避免越权修改或删除他人购物车数据。
     */
    private void validateCartItemOwner(Long userId, CartItem cartItem) {
        if (!userId.equals(cartItem.getUserId())) {
            throw new BizException(403, "无权操作该购物车项");
        }
    }
}
