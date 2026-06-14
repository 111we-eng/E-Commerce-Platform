package com.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.order.dto.Result;
import com.order.entity.Order;
import com.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理 REST API
 *
 * CRUD 接口：
 * - POST   /api/orders          创建订单
 * - GET    /api/orders          分页查询（?page=1&size=10&status=PENDING）
 * - GET    /api/orders/{id}     查询详情（含明细）
 * - PUT    /api/orders/{id}     更新订单
 * - PUT    /api/orders/{id}/status  更新状态
 * - DELETE /api/orders/{id}     删除订单
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ==================== 创建 ====================

    /**
     * 创建订单（含明细）
     *
     * 请求体示例：
     * {
     *   "userId": 1,
     *   "receiverName": "张三",
     *   "receiverPhone": "13800000001",
     *   "receiverAddress": "北京市朝阳区",
     *   "remark": "请尽快发货",
     *   "items": [
     *     { "productId": 101, "productName": "MacBook Pro", "productPrice": 14999.00, "quantity": 1 }
     *   ]
     * }
     */
    @PostMapping
    public Result<Order> create(@RequestBody Order order) {
        if (order.getUserId() == null) {
            return Result.fail(400, "用户ID不能为空");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return Result.fail(400, "订单商品不能为空");
        }
        Order created = orderService.createOrder(order);
        return Result.ok("订单创建成功", created);
    }

    // ==================== 查询 ====================

    /**
     * 分页查询订单列表
     *
     * @param page   页码（默认1）
     * @param size   每页条数（默认10）
     * @param status 状态筛选（可选：PENDING/PAID/SHIPPED/COMPLETED/CANCELLED）
     */
    @GetMapping
    public Result<IPage<Order>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        IPage<Order> result = orderService.pageOrders(page, size, status);
        return Result.ok(result);
    }

    /**
     * 按用户ID查询订单
     */
    @GetMapping("/user/{userId}")
    public Result<IPage<Order>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Order> result = orderService.pageByUserId(userId, page, size);
        return Result.ok(result);
    }

    /**
     * 查询订单详情（含订单明细）
     */
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.fail(404, "订单不存在");
        }
        return Result.ok(order);
    }

    // ==================== 更新 ====================

    /**
     * 更新订单信息（收货信息、备注等）
     */
    @PutMapping("/{id}")
    public Result<Order> update(@PathVariable Long id, @RequestBody Order order) {
        Order existing = orderService.getById(id);
        if (existing == null) {
            return Result.fail(404, "订单不存在");
        }
        order.setId(id);
        orderService.updateById(order);
        Order updated = orderService.getOrderDetail(id);
        return Result.ok("订单更新成功", updated);
    }

    /**
     * 更新订单状态
     *
     * 请求体示例：{ "status": "PAID" }
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Order request) {
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            return Result.fail(400, "状态不能为空");
        }
        Order existing = orderService.getById(id);
        if (existing == null) {
            return Result.fail(404, "订单不存在");
        }
        orderService.updateOrderStatus(id, request.getStatus());
        return Result.ok("状态更新成功", null);
    }

    // ==================== 删除 ====================

    /**
     * 删除订单（同时级联删除订单明细）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Order existing = orderService.getById(id);
        if (existing == null) {
            return Result.fail(404, "订单不存在");
        }
        orderService.deleteOrder(id);
        return Result.ok("订单删除成功", null);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量删除订单
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(400, "请选择要删除的订单");
        }
        for (Long id : ids) {
            orderService.deleteOrder(id);
        }
        return Result.ok("批量删除成功", null);
    }
}
