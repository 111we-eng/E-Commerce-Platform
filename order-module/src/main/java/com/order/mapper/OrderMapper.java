package com.order.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.order.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单 Mapper — 继承 MyBatis Plus BaseMapper 获得 CRUD 能力
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单（含明细）
     * 使用 LEFT JOIN 一次性查出订单 + 明细，再通过 resultMap 映射
     */
    @Select("SELECT DISTINCT o.* FROM orders o " +
            "LEFT JOIN order_items oi ON o.id = oi.order_id " +
            "${ew.customSqlSegment}")
    IPage<Order> selectPageWithItems(IPage<Order> page, @Param(Constants.WRAPPER) Wrapper<Order> wrapper);

    /**
     * 查询单个订单（含明细）
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    @Results(id = "orderResultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "orderNo", column = "order_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "receiverName", column = "receiver_name"),
            @Result(property = "receiverPhone", column = "receiver_phone"),
            @Result(property = "receiverAddress", column = "receiver_address"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "items", column = "id",
                    many = @Many(select = "com.order.mapper.OrderItemMapper.selectByOrderId"))
    })
    Order selectByIdWithItems(Long id);

    /**
     * 按用户ID查询订单列表（含明细）
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    @ResultMap("orderResultMap")
    List<Order> selectByUserIdWithItems(@Param("userId") Long userId);

    /**
     * 按状态统计数量
     */
    @Select("SELECT status, COUNT(*) as cnt FROM orders GROUP BY status")
    List<java.util.Map<String, Object>> countByStatus();
}
