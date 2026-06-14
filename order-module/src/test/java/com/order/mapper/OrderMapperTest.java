package com.order.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mapper 层测试
 *
 * 验证 MyBatis Plus BaseMapper 提供的 CRUD 方法及自定义 SQL
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    private static Long testOrderId;

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("1. 插入订单 — BaseMapper.insert()")
    void testInsert() {
        Order order = new Order();
        order.setOrderNo("TEST20260614001");
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("2999.00"));
        order.setStatus("PENDING");
        order.setReceiverName("测试用户");
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("测试地址");

        int rows = orderMapper.insert(order);
        assertEquals(1, rows);
        assertNotNull(order.getId(), "插入后 ID 应自动回填");

        testOrderId = order.getId();
        System.out.println("  [PASS] 插入订单成功, ID = " + testOrderId);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("2. 按ID查询 — BaseMapper.selectById()")
    void testSelectById() {
        Order order = orderMapper.selectById(testOrderId);
        assertNotNull(order);
        assertEquals("TEST20260614001", order.getOrderNo());
        assertEquals(new BigDecimal("2999.00"), order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
        System.out.println("  [PASS] 查询订单: " + order.getOrderNo());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("3. 分页查询 — BaseMapper.selectPage()")
    void testSelectPage() {
        Page<Order> page = new Page<>(1, 5);
        IPage<Order> result = orderMapper.selectPage(page, null);
        assertTrue(result.getTotal() > 0, "应至少有一条数据");
        System.out.println("  [PASS] 分页查询: 总数=" + result.getTotal() + ", 当前页=" + result.getRecords().size() + "条");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("4. 查询订单详情（含明细）")
    void testSelectByIdWithItems() {
        // 先插入一条明细
        OrderItem item = new OrderItem();
        item.setOrderId(testOrderId);
        item.setProductId(101L);
        item.setProductName("测试商品");
        item.setProductPrice(new BigDecimal("2999.00"));
        item.setQuantity(1);
        orderItemMapper.insert(item);

        Order order = orderMapper.selectByIdWithItems(testOrderId);
        assertNotNull(order);
        assertNotNull(order.getItems());
        assertTrue(order.getItems().size() > 0);
        System.out.println("  [PASS] 订单详情含明细: " + order.getItems().size() + "条明细");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("5. 更新订单 — BaseMapper.updateById()")
    void testUpdate() {
        Order order = new Order();
        order.setId(testOrderId);
        order.setStatus("PAID");
        order.setReceiverName("更新后的名字");

        int rows = orderMapper.updateById(order);
        assertEquals(1, rows);

        Order updated = orderMapper.selectById(testOrderId);
        assertEquals("PAID", updated.getStatus());
        assertEquals("更新后的名字", updated.getReceiverName());
        System.out.println("  [PASS] 更新订单状态: " + updated.getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("6. 按用户ID查询")
    void testSelectByUserId() {
        List<Order> orders = orderMapper.selectByUserIdWithItems(1L);
        assertNotNull(orders);
        System.out.println("  [PASS] 用户ID=1的订单: " + orders.size() + "条");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("7. 删除订单明细 + 删除订单")
    void testDelete() {
        orderItemMapper.deleteByOrderId(testOrderId);
        int rows = orderMapper.deleteById(testOrderId);
        assertEquals(1, rows);

        Order deleted = orderMapper.selectById(testOrderId);
        assertNull(deleted);
        System.out.println("  [PASS] 删除订单成功, ID=" + testOrderId);
    }
}
