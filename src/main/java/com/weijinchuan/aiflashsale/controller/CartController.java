package com.weijinchuan.aiflashsale.controller;

import com.weijinchuan.aiflashsale.common.api.Result;
import com.weijinchuan.aiflashsale.dto.cart.AddCartItemDTO;
import com.weijinchuan.aiflashsale.dto.cart.UpdateCartItemDTO;
import com.weijinchuan.aiflashsale.service.CartService;
import com.weijinchuan.aiflashsale.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车接口控制器
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 加入购物车
     */
    @PostMapping("/items")
    @Operation(summary = "加入购物车")
    public Result<Void> addItem(@Valid @RequestBody AddCartItemDTO dto) {
        cartService.addItem(dto);
        return Result.success();
    }

    /**
     * 查询购物车
     */
    @GetMapping
    @Operation(summary = "查询购物车")
    public Result<CartVO> getCart(@RequestParam Long userId,
                                  @RequestParam Long storeId) {
        return Result.success(cartService.getCart(userId, storeId));
    }

    /**
     * 修改购物车项数量
     */
    @PutMapping("/items/{itemId}")
    @Operation(summary = "修改购物车项数量")
    public Result<Void> updateItemQuantity(@PathVariable Long itemId,
                                           @Valid @RequestBody UpdateCartItemDTO dto) {
        cartService.updateItemQuantity(itemId, dto);
        return Result.success();
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "删除购物车项")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        cartService.deleteItem(itemId);
        return Result.success();
    }
}