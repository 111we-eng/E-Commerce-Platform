package com.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.entity.Order;
import com.order.entity.OrderItem;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 层集成测试 — 使用 @SpringBootTest 完整启动上下文
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long testOrderId;

    // ==================== 查询测试 ====================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("1. GET /api/orders — 分页查询")
    void testListOrders() throws Exception {
        mockMvc.perform(get("/api/orders").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.records").isArray());
        System.out.println("  [PASS] 分页查询成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("2. GET /api/orders?status=PENDING — 按状态筛选")
    void testListOrdersByStatus() throws Exception {
        mockMvc.perform(get("/api/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].status").value("PENDING"));
        System.out.println("  [PASS] 状态筛选成功");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("3. GET /api/orders/1 — 查询订单详情（含明细）")
    void testGetOrderDetail() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.items").isArray());
        System.out.println("  [PASS] 订单详情查询成功（含明细）");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("4. GET /api/orders/999 — 不存在的订单返回404")
    void testGetOrderDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
        System.out.println("  [PASS] 不存在的订单返回404");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("5. GET /api/orders/user/1 — 按用户查询")
    void testListByUser() throws Exception {
        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("  [PASS] 按用户查询成功");
    }

    // ==================== 创建测试 ====================

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("6. POST /api/orders — 创建订单")
    void testCreateOrder() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("userId", 1);
        request.put("receiverName", "Controller测试");
        request.put("receiverPhone", "13800000000");
        request.put("receiverAddress", "北京市测试地址");
        request.put("remark", "Controller集成测试");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("productId", 301);
        item.put("productName", "测试商品X");
        item.put("productPrice", 99.00);
        item.put("quantity", 2);
        items.add(item);
        request.put("items", items);

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.totalAmount").value(198.0))
                .andReturn().getResponse().getContentAsString();

        // 提取订单ID用于后续删除测试
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        testOrderId = Long.valueOf(((Map<String, Object>) result.get("data")).get("id").toString());
        System.out.println("  [PASS] 创建订单成功: ID=" + testOrderId);
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("7. POST /api/orders — 参数校验（无userId）")
    void testCreateOrderValidationFail() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("items", Collections.emptyList());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
        System.out.println("  [PASS] 参数校验返回400");
    }

    // ==================== 更新测试 ====================

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("8. PUT /api/orders/{id}/status — 更新订单状态")
    void testUpdateOrderStatus() throws Exception {
        Map<String, String> statusRequest = new LinkedHashMap<>();
        statusRequest.put("status", "SHIPPED");

        mockMvc.perform(put("/api/orders/3/status")  // 使用预置订单3
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("  [PASS] 状态更新成功");

        // 恢复
        statusRequest.put("status", "SHIPPED");
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("9. PUT /api/orders/999/status — 更新不存在订单")
    void testUpdateStatusNotFound() throws Exception {
        Map<String, String> statusRequest = new LinkedHashMap<>();
        statusRequest.put("status", "PAID");

        mockMvc.perform(put("/api/orders/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
        System.out.println("  [PASS] 不存在订单返回404");
    }

    // ==================== 删除测试 ====================

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("10. DELETE /api/orders/{id} — 删除订单")
    void testDeleteOrder() throws Exception {
        assertNotNull(testOrderId, "需要先创建订单");
        mockMvc.perform(delete("/api/orders/" + testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("  [PASS] 订单删除成功: ID=" + testOrderId);
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("11. DELETE /api/orders/999 — 删除不存在订单")
    void testDeleteNotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
        System.out.println("  [PASS] 删除不存在订单返回404");
    }
}
