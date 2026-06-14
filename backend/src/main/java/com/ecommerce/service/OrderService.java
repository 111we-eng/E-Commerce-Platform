package com.ecommerce.service;

import com.ecommerce.domain.Order;
import com.ecommerce.dto.OrderCreateRequest;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, OrderCreateRequest request);
    Order getOrderById(Long id);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getAllOrders();   // 管理员
    void updateOrderStatus(Long id, String status);
}
