package com.ecommerce.repository;

import com.ecommerce.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    Order findById(@Param("id") Long id);
    List<Order> findByUserId(@Param("userId") Long userId);
    List<Order> findAll();  // 管理员查看全部订单
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
