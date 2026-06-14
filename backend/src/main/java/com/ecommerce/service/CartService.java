package com.ecommerce.service;

import com.ecommerce.domain.CartItem;

import java.util.List;

public interface CartService {
    List<CartItem> getCartByUserId(Long userId);
    CartItem addToCart(Long userId, Long productId, Integer quantity);
    CartItem updateQuantity(Long id, Integer quantity);
    void removeFromCart(Long id);
    void clearCart(Long userId);
}
