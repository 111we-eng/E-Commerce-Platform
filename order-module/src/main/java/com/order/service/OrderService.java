package com.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.order.entity.Order;

/**
 * 订单 Service 接口
 *
 * MyBatis Plus IService 提供：
 * - save/update/remove/getById/list 等近 20 个 CRUD 方法
 * - 无需手写基础 CRUD 代码
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单（含明细，事务）
     */
    Order createOrder(Order order);

    /**
     * 分页查询订单列表（含明细）
     */
    IPage<Order> pageOrders(int pageNum, int pageSize, String status);

    /**
     * 按用户ID查询订单列表
     */
    IPage<Order> pageByUserId(Long userId, int pageNum, int pageSize);

    /**
     * 查询订单详情（含明细）
     */
    Order getOrderDetail(Long id);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long id, String status);

    /**
     * 删除订单（级联删除明细）
     */
    boolean deleteOrder(Long id);
}
