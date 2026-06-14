package com.ecommerce.controller;

import com.ecommerce.domain.CartItem;
import com.ecommerce.dto.Result;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取当前用户购物车
     */
    @GetMapping
    public Result<List<CartItem>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(cartService.getCartByUserId(userId));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public Result<CartItem> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = body.containsKey("quantity") ?
                Integer.valueOf(body.get("quantity").toString()) : 1;
        CartItem item = cartService.addToCart(userId, productId, quantity);
        return Result.ok("已添加到购物车", item);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(id, body.get("quantity"));
        return Result.ok("更新成功", null);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return Result.ok("已删除", null);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public Result<Void> clear(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.ok("购物车已清空", null);
    }
}
