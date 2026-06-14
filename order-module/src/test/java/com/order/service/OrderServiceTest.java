package com.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service 层集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("1. Service 可用性检查")
    void testServiceAvailable() {
        assertNotNull(orderService, "OrderService 应已注入");
        System.out.println("  [PASS] OrderService 注入成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("2. 分页查询 — pageOrders()")
    void testPageOrders() {
        // 查询所有
        IPage<Order> all = orderService.pageOrders(1, 10, null);
        assertNotNull(all);
        assertTrue(all.getTotal() >= 5, "预置数据至少5条，实际: " + all.getTotal());
        System.out.println("  [PASS] 分页查询所有: " + all.getTotal() + " 条");

        // 按状态筛选
        IPage<Order> pending = orderService.pageOrders(1, 10, "PENDING");
        assertNotNull(pending);
        for (Order o : pending.getRecords()) {
            assertEquals("PENDING", o.getStatus());
        }
        System.out.println("  [PASS] 按状态筛选 PENDING: " + pending.getTotal() + " 条");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("3. 查询详情 — getOrderDetail()")
    void testGetOrderDetail() {
        // 查询ID=1的预置订单
        Order order = orderService.getOrderDetail(1L);
        assertNotNull(order);
        assertNotNull(order.getOrderNo());
        assertNotNull(order.getItems(), "应包含订单明细");
        assertTrue(order.getItems().size() > 0, "明细至少1条");
        System.out.println("  [PASS] 订单1详情: " + order.getOrderNo() + ", 明细 " + order.getItems().size() + " 条");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("4. 按用户查询 — pageByUserId()")
    void testPageByUserId() {
        IPage<Order> result = orderService.pageByUserId(1L, 1, 10);
        assertNotNull(result);
        assertTrue(result.getTotal() > 0, "用户1应有订单");
        for (Order o : result.getRecords()) {
            assertEquals(1L, (long) o.getUserId());
        }
        System.out.println("  [PASS] 用户1的订单: " + result.getTotal() + " 条");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("5. MyBatis Plus 内置方法验证")
    void testBaseMapperMethods() {
        long count = orderService.count();
        assertTrue(count >= 5, "至少应有5条数据");
        System.out.println("  [PASS] count(): " + count);

        List<Order> all = orderService.list();
        assertNotNull(all);
        assertTrue(all.size() >= 5);
        System.out.println("  [PASS] list(): " + all.size() + " 条");

        Order first = orderService.getById(1L);
        assertNotNull(first);
        assertNotNull(first.getOrderNo());
        System.out.println("  [PASS] getById(1): " + first.getOrderNo());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("6. 查询不存在的订单")
    void testGetByIdNotFound() {
        Order order = orderService.getById(99999L);
        assertNull(order);
        System.out.println("  [PASS] 不存在的订单返回 null");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("7. 创建新订单 — createOrder()")
    void testCreateOrder() {
        Order order = new Order();
        order.setUserId(2L);
        order.setReceiverName("测试用户");
        order.setReceiverPhone("13900000001");
        order.setReceiverAddress("测试地址");
        order.setRemark("自动化测试订单");

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setProductId(201L);
        item.setProductName("测试商品A");
        item.setProductPrice(new BigDecimal("100.00"));
        item.setQuantity(3);
        items.add(item);

        OrderItem item2 = new OrderItem();
        item2.setProductId(202L);
        item2.setProductName("测试商品B");
        item2.setProductPrice(new BigDecimal("200.00"));
        item2.setQuantity(1);
        items.add(item2);

        order.setItems(items);

        Order created = orderService.createOrder(order);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getOrderNo().startsWith("ORD"));
        assertEquals(new BigDecimal("500.00"), created.getTotalAmount());
        assertEquals("PENDING", created.getStatus());
        assertEquals(2, created.getItems().size());
        System.out.println("  [PASS] 创建订单: " + created.getOrderNo() + ", 金额: " + created.getTotalAmount());

        // 更新状态
        orderService.updateOrderStatus(created.getId(), "PAID");
        Order updated = orderService.getById(created.getId());
        assertEquals("PAID", updated.getStatus());
        System.out.println("  [PASS] 状态更新: PENDING -> PAID");

        // 删除订单
        boolean deleted = orderService.deleteOrder(created.getId());
        assertTrue(deleted);
        assertNull(orderService.getById(created.getId()));
        System.out.println("  [PASS] 订单已删除");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("8. 更新订单状态 — updateOrderStatus()")
    void testUpdateOrderStatus() {
        // 使用预置订单 ID=2 (PAID)，更新为 SHIPPED
        boolean result = orderService.updateOrderStatus(2L, "SHIPPED");
        assertTrue(result);
        Order updated = orderService.getById(2L);
        assertEquals("SHIPPED", updated.getStatus());
        System.out.println("  [PASS] 订单2状态: PAID -> SHIPPED");

        // 恢复原状态
        orderService.updateOrderStatus(2L, "PAID");
    }
}
