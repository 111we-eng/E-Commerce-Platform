package com.ecommerce.repository;

import com.ecommerce.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem item);
    int insertBatch(@Param("items") List<OrderItem> items);
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
}
