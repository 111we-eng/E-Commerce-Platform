package com.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import com.order.mapper.OrderItemMapper;
import com.order.mapper.OrderMapper;
import com.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 订单 Service 实现
 *
 * 继承 ServiceImpl<OrderMapper, Order> 获得 MyBatis Plus 内置 CRUD：
 * - save(entity)         → INSERT
 * - saveBatch(list)      → 批量 INSERT
 * - updateById(entity)   → UPDATE BY ID
 * - removeById(id)       → DELETE BY ID
 * - getById(id)          → SELECT BY ID
 * - list()               → SELECT ALL
 * - page(page, wrapper)  → 分页查询
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 1. 生成订单编号
        order.setOrderNo(generateOrderNo());

        // 2. 计算总金额
        BigDecimal total = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                BigDecimal price = item.getProductPrice() != null ? item.getProductPrice() : BigDecimal.ZERO;
                total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0)));
            }
        }
        order.setTotalAmount(total);
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }

        // 3. 保存订单主表
        orderMapper.insert(order);

        // 4. 保存订单明细
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }
        }

        // 5. 回查完整订单
        return orderMapper.selectByIdWithItems(order.getId());
    }

    @Override
    public IPage<Order> pageOrders(int pageNum, int pageSize, String status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        // MyBatis Plus 分页查询
        IPage<Order> result = orderMapper.selectPage(page, wrapper);

        // 为每个订单填充明细
        for (Order order : result.getRecords()) {
            order.setItems(orderItemMapper.selectByOrderId(order.getId()));
        }

        return result;
    }

    @Override
    public IPage<Order> pageByUserId(Long userId, int pageNum, int pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
               .orderByDesc(Order::getCreateTime);

        IPage<Order> result = orderMapper.selectPage(page, wrapper);
        for (Order order : result.getRecords()) {
            order.setItems(orderItemMapper.selectByOrderId(order.getId()));
        }
        return result;
    }

    @Override
    public Order getOrderDetail(Long id) {
        return orderMapper.selectByIdWithItems(id);
    }

    @Override
    public boolean updateOrderStatus(Long id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        // 级联删除明细
        orderItemMapper.deleteByOrderId(id);
        // 删除订单主表
        return orderMapper.deleteById(id) > 0;
    }

    // ===== 私有方法 =====

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
