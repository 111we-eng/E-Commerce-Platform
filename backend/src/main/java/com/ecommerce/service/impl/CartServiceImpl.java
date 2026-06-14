package com.ecommerce.service.impl;

import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.Product;
import com.ecommerce.repository.CartMapper;
import com.ecommerce.repository.ProductMapper;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<CartItem> getCartByUserId(Long userId) {
        return cartMapper.findByUserId(userId);
    }

    @Override
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在且有库存
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + product.getStock());
        }

        // 检查购物车中是否已有该商品
        CartItem existing = cartMapper.findByUserIdAndProductId(userId, productId);
        if (existing != null) {
            // 已有则累加数量
            int newQty = existing.getQuantity() + (quantity != null ? quantity : 1);
            cartMapper.updateQuantity(existing.getId(), newQty);
            return cartMapper.findByUserIdAndProductId(userId, productId);
        }

        // 新增
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity != null ? quantity : 1);
        cartMapper.insert(item);

        return cartMapper.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public CartItem updateQuantity(Long id, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        cartMapper.updateQuantity(id, quantity);
        return null;
    }

    @Override
    public void removeFromCart(Long id) {
        cartMapper.deleteById(id);
    }

    @Override
    public void clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
    }
}
