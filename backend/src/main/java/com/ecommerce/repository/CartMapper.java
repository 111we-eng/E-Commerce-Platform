package com.ecommerce.repository;

import com.ecommerce.domain.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    List<CartItem> findByUserId(@Param("userId") Long userId);
    CartItem findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    int insert(CartItem item);
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    int deleteById(@Param("id") Long id);
    int deleteByUserId(@Param("userId") Long userId);
}
