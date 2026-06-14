package com.ecommerce.service.impl;

import com.ecommerce.domain.Order;
import com.ecommerce.domain.OrderItem;
import com.ecommerce.domain.Product;
import com.ecommerce.dto.OrderCreateRequest;
import com.ecommerce.repository.OrderItemMapper;
import com.ecommerce.repository.OrderMapper;
import com.ecommerce.repository.ProductMapper;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Order createOrder(Long userId, OrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("订单商品不能为空");
        }

        // 生成订单编号
        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 计算订单总价，构建订单明细
        for (OrderCreateRequest.CartItemRequest itemReq : request.getItems()) {
            Product product = productMapper.findById(itemReq.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在 (ID=" + itemReq.getProductId() + ")");
            }
            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足");
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(product.getPrice());
            orderItems.add(item);

            // 扣减库存
            product.setStock(product.getStock() - itemReq.getQuantity());
            productMapper.update(product);
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setAddress(request.getAddress());
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        // 插入订单明细
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        order.setItems(orderItems);
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderMapper.findAll();
    }

    @Override
    public void updateOrderStatus(Long id, String status) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        orderMapper.updateStatus(id, status);
    }
}
