package com.ecommerce.controller;

import com.ecommerce.domain.Order;
import com.ecommerce.dto.OrderCreateRequest;
import com.ecommerce.dto.Result;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单（下单）
     *
     * 交互流程：
     * 1. 前端 POST /api/orders 发送 {address, items: [{productId, quantity}]}
     * 2. JwtAuthInterceptor 拦截 → 解析 Token → 提取 userId
     * 3. Controller 从 request.getAttribute("userId") 获取用户ID
     * 4. OrderService.createOrder() 执行下单逻辑：
     *    a. 验证商品库存
     *    b. 生成订单编号
     *    c. 计算总价
     *    d. 扣减库存
     *    e. 写入 orders + order_item
     * 5. 返回完整订单信息
     */
    @PostMapping
    public Result<Order> create(@RequestBody OrderCreateRequest orderRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.createOrder(userId, orderRequest);
        return Result.ok("下单成功", order);
    }

    /**
     * 查询当前用户的所有订单
     *
     * 交互流程：
     * 1. 前端 GET /api/orders
     * 2. JWT 拦截器解析 Token → 提取 userId
     * 3. OrderMapper.findByUserId() 查询订单列表（含 order_item 明细）
     * 4. 返回 Result<List<Order>>（List 中每个 Order 包含 items 列表）
     */
    @GetMapping
    public Result<List<Order>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.getOrdersByUserId(userId));
    }

    /**
     * 查询单个订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getOrderById(id);
        // 只能查看自己的订单，管理员除外
        if (!order.getUserId().equals(userId)) {
            return Result.fail(403, "无权查看此订单");
        }
        return Result.ok(order);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getOrderById(id);
        if (!order.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return Result.fail(400, "只有待支付订单可以取消");
        }
        orderService.updateOrderStatus(id, "CANCELLED");
        return Result.ok("订单已取消", null);
    }

    /**
     * 管理员获取全部订单
     */
    @GetMapping("/all")
    public Result<List<Order>> all(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.fail(403, "无权限");
        }
        return Result.ok(orderService.getAllOrders());
    }

    /**
     * 管理员更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateOrderStatus(id, body.get("status"));
        return Result.ok("状态更新成功", null);
    }
}
