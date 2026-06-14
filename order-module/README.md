# 📦 订单模块 CRUD — Spring Boot 3 + MyBatis Plus

> 基于 **Spring Boot 3 + MyBatis Plus** 实现电商数据库订单模块的完整 CRUD 接口，包含三层测试（Mapper / Service / Controller）。

---

## (1) 实现思路

### 技术选型

| 组件 | 版本 | 选型理由 |
|------|------|---------|
| Spring Boot | 3.2.0 | 最新稳定版，需 Java 17+ |
| MyBatis Plus | 3.5.5 | `mybatis-plus-spring-boot3-starter` 支持 Spring Boot 3 |
| H2 Database | 2.2 | 内存数据库，测试零配置 |
| JUnit 5 | 5.10 | Spring Boot 3 默认测试框架 |
| MockMvc | — | Spring Boot Test 内置，模拟 HTTP 请求 |

### 架构设计

```
┌─────────────────────────────────────────┐
│  Controller 层 (REST API)               │
│  OrderController                        │
│  - 参数校验 (@Valid/手动)               │
│  - 调用 Service                         │
│  - 返回统一 Result<T>                   │
├─────────────────────────────────────────┤
│  Service 层 (业务逻辑)                  │
│  OrderService extends IService<Order>   │
│  OrderServiceImpl                       │
│  - 继承 ServiceImpl 获得内置 CRUD       │
│  - @Transactional 事务管理              │
│  - 级联操作订单+明细                    │
├─────────────────────────────────────────┤
│  Mapper 层 (数据访问)                   │
│  OrderMapper extends BaseMapper<Order>  │
│  OrderItemMapper                        │
│  - MyBatis Plus 自动提供 CRUD           │
│  - @Select/@Insert 自定义 SQL           │
│  - 分页查询 (IPage)                     │
├─────────────────────────────────────────┤
│  Entity 层                              │
│  Order / OrderItem                      │
│  - @TableName 表映射                    │
│  - @TableId(type=IdType.AUTO) 主键策略  │
│  - @TableField(exist=false) 关联字段    │
└─────────────────────────────────────────┘
```

### MyBatis Plus 核心优势

- **BaseMapper<T>**：内置 `insert`/`deleteById`/`updateById`/`selectById`/`selectPage` 等近 20 个方法
- **IService<T> + ServiceImpl**：Service 层同样获得内置 CRUD，无需手写基础代码
- **LambdaQueryWrapper**：类型安全的条件构造器，避免字段名字符串硬编码
- **分页插件**：`PaginationInnerInterceptor` 一行配置即可启用物理分页
- **自动填充**：`@TableField(fill=...)` 配合 `MetaObjectHandler` 自动设置 createTime/updateTime

---

## (2) 表结构 SQL

```sql
-- 订单主表
CREATE TABLE orders (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no         VARCHAR(32)   NOT NULL UNIQUE COMMENT '订单编号',
    user_id          BIGINT        NOT NULL COMMENT '用户ID',
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING' 
                     COMMENT 'PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    receiver_name    VARCHAR(50)   COMMENT '收货人',
    receiver_phone   VARCHAR(20)   COMMENT '收货电话',
    receiver_address VARCHAR(255)  COMMENT '收货地址',
    remark           VARCHAR(500)  COMMENT '备注',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP
);

-- 订单明细表
CREATE TABLE order_items (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id        BIGINT  NOT NULL COMMENT '订单ID',
    product_id      BIGINT  NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(200) COMMENT '商品名称',
    product_price   DECIMAL(10,2) COMMENT '商品单价',
    quantity        INT     NOT NULL DEFAULT 1 COMMENT '数量',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
```

> 完整 SQL 含测试数据见：`src/main/resources/db/schema.sql`

---

## (3) 接口代码

### CRUD API 列表

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/orders` | 创建订单（含明细） |
| `GET` | `/api/orders?page=1&size=10` | 分页查询 |
| `GET` | `/api/orders?status=PENDING` | 按状态筛选 |
| `GET` | `/api/orders/{id}` | 查询详情（含明细） |
| `GET` | `/api/orders/user/{userId}` | 按用户查询 |
| `PUT` | `/api/orders/{id}` | 更新订单 |
| `PUT` | `/api/orders/{id}/status` | 更新状态 |
| `DELETE` | `/api/orders/{id}` | 删除订单（级联） |
| `DELETE` | `/api/orders/batch` | 批量删除 |

### 核心代码片段

```java
// MyBatis Plus BaseMapper — 一行代码获得 CRUD
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // 自定义分页查询（含明细）
    @Select("SELECT * FROM orders WHERE id = #{id}")
    @Results(...)
    Order selectByIdWithItems(Long id);
}

// MyBatis Plus ServiceImpl — 继承获得内置 CRUD
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Override
    @Transactional
    public Order createOrder(Order order) {
        order.setOrderNo(generateOrderNo());           // 生成订单号
        order.setTotalAmount(calculateTotal(order));   // 计算总额
        orderMapper.insert(order);                     // MP 内置 insert
        // 批量插入明细
        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return orderMapper.selectByIdWithItems(order.getId());
    }

    @Override
    public IPage<Order> pageOrders(int page, int size, String status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // 类型安全的条件构造 — 无字符串硬编码
        wrapper.eq(StringUtils.hasText(status), Order::getStatus, status)
               .orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }
}
```

> 完整代码见：`src/main/java/com/order/`

---

## (4) 测试报告

### 测试策略

| 层级 | 注解 | 数据库 | 测试数 | 说明 |
|------|------|--------|--------|------|
| **Mapper** | `@SpringBootTest` | H2 内存库 | 7 | 验证 SQL 映射、CRUD、分页 |
| **Service** | `@SpringBootTest` | H2 内存库 | 8 | 验证业务逻辑、事务、级联操作 |
| **Controller** | `@SpringBootTest + @AutoConfigureMockMvc` | H2 内存库 | 11 | 验证 URL 映射、参数校验、JSON 响应 |
| **Application** | `@SpringBootTest` | — | 1 | 验证 Spring 容器启动 |

### 测试结果汇总

```
╔══════════════════════════════════════════╤══════╤════════╤══════════╤══════╗
║ 测试类                                   │ 测试 │ 失败   │ 错误     │ 耗时  ║
╠══════════════════════════════════════════╪══════╪════════╪══════════╪══════╣
║ OrderMapperTest                         │   7  │    0   │    0     │ 0.48s║
║ OrderServiceTest                        │   8  │    0   │    0     │ 0.09s║
║ OrderControllerTest                     │  11  │    0   │    0     │ 3.40s║
║ OrderModuleApplicationTests             │   1  │    0   │    0     │ 0.01s║
╠══════════════════════════════════════════╪══════╪════════╪══════════╪══════╣
║ TOTAL                                   │  27  │    0   │    0     │ 3.98s║
╚══════════════════════════════════════════╧══════╧════════╧══════════╧══════╝
```

### 测试覆盖的接口

| # | 测试方法 | 验证点 |
|---|---------|--------|
| 1 | `testListOrders` | 分页查询返回正确结构 |
| 2 | `testListOrdersByStatus` | 按 PENDING 筛选 |
| 3 | `testGetOrderDetail` | 详情含明细 items |
| 4 | `testGetOrderDetailNotFound` | 不存在返回 404 |
| 5 | `testListByUser` | 按用户 ID 查询 |
| 6 | `testCreateOrder` | 创建订单 + 金额计算 + 明细保存 |
| 7 | `testCreateOrderValidationFail` | 缺少 userId 返回 400 |
| 8 | `testUpdateOrderStatus` | 状态更新 |
| 9 | `testUpdateStatusNotFound` | 不存在返回 404 |
| 10 | `testDeleteOrder` | 删除已创建的测试订单 |
| 11 | `testDeleteNotFound` | 删除不存在返回 404 |

---

## 🚀 快速启动

```bash
# 1. 运行测试
mvn clean test

# 2. 启动应用（H2 内存数据库，零配置）
mvn spring-boot:run

# 3. 访问 H2 控制台（可选）
# http://localhost:8089/h2-console
# JDBC URL: jdbc:h2:mem:orders
# User/Pass: sa / (空)

# 4. 测试 API
curl http://localhost:8089/api/orders?page=1&size=5
curl http://localhost:8089/api/orders/1
```

### 切换 MySQL

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

## 📁 项目结构

```
order-module/
├── pom.xml
├── src/main/java/com/order/
│   ├── OrderApplication.java
│   ├── entity/Order.java, OrderItem.java
│   ├── mapper/OrderMapper.java, OrderItemMapper.java
│   ├── service/OrderService.java + impl/OrderServiceImpl.java
│   ├── controller/OrderController.java
│   ├── dto/Result.java
│   └── config/MyBatisPlusConfig.java
├── src/main/resources/
│   ├── application.yml, application-mysql.yml
│   └── db/schema.sql
├── src/test/java/com/order/
│   ├── OrderModuleApplicationTests.java
│   ├── mapper/OrderMapperTest.java
│   ├── service/OrderServiceTest.java
│   └── controller/OrderControllerTest.java
└── README.md
```

---

## (5) 开发过程中的 AI 提问

| # | 提问 | 决策 |
|---|------|------|
| 1 | Spring Boot 3 与 MyBatis Plus 如何整合？ | 使用 `mybatis-plus-spring-boot3-starter:3.5.5`，兼容 Spring Boot 3.x |
| 2 | Java 版本选多少？ | 选 Java 17，Spring Boot 3 最低要求 |
| 3 | 测试用什么数据库？ | H2 内存数据库，零配置，与 MySQL 高度兼容 |
| 4 | Lombok 在 JDK 24 下不兼容怎么办？ | 改用手写 getter/setter，移除 @Data 注解 |
| 5 | Mapper 测试如何避免脏数据？ | 用自增 ID 隔离，测试创建的数据在独立方法中创建和清理 |
| 6 | Controller 测试报上下文加载错误？ | 从 @WebMvcTest 改为 @SpringBootTest + @AutoConfigureMockMvc |
| 7 | 分页如何实现？ | MyBatis Plus `PaginationInnerInterceptor` + `Page<T>` |
| 8 | 订单号如何生成？ | `ORD + 时间戳 + UUID前6位`，保证唯一性 |
| 9 | 订单明细如何关联查询？ | `@Result(property="items", many=@Many(select=...))` 嵌套查询 |
| 10 | 删除订单时明细如何处理？ | 使用 `ON DELETE CASCADE` 数据库级联删除 |

---

## 📜 License

MIT License
