# 🛒 电商购物平台

> 一个完整的前后端分离电商购物平台，支持商品浏览、购物车管理、订单流程，采用 **JWT Token** 实现无状态用户认证。

---

## 📂 项目概览

| 项目 | 说明 |
|------|------|
| **项目名称** | 电商购物平台（E-Commerce Platform） |
| **架构** | 前后端分离 — Vue 3 + Spring Boot |
| **认证方式** | JWT Token（JSON Web Token）无状态认证 |
| **数据库** | SQLite（默认开发），支持切换到 MySQL |

---

## ✨ 功能特性

- 🔐 **JWT Token 登录** — 用户注册/登录，Token 签名验证，过期自动跳转登录页
- 🛍️ **商品浏览** — 商品列表、分类筛选、关键词搜索、商品详情
- 🛒 **购物车** — 添加商品、修改数量、删除、选中结算
- 📦 **订单管理** — 创建订单、订单列表查询、订单详情、取消订单、状态跟踪
- 👤 **个人中心** — 查看用户信息、订单统计
- 🛡️ **路由守卫** — 未登录自动拦截，登录态跳转优化
- 📡 **RESTful API** — 统一响应格式 `{code, message, data}`

---

## 🛠️ 技术栈

| 层 | 技术 |
|----|------|
| **后端** | Java 8, Spring Boot 2.2.13, MyBatis 3.5, JWT (jjwt 0.9.1), Druid 1.2.6 |
| **前端** | Vue 3.4, Vue Router 4, Element Plus 2.5, Axios 1.6, Vite 5 |
| **数据库** | SQLite（默认） / MySQL 8.0 |

---

## 🚀 快速启动

### 环境要求
- JDK 1.8+  /  Maven 3.0+  /  Node.js 16+

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 **http://localhost:8088**

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 **http://localhost:5173**

### 3. 访问系统

- 🔗 地址：**http://localhost:5173**
- 👤 管理员：`admin` / `admin123`
- 👤 普通用户：`user` / `admin123`

---

## 📁 项目结构

```
ecommerce-platform/
├── backend/                              ← Spring Boot 后端
│   ├── src/main/java/com/ecommerce/
│   │   ├── config/
│   │   │   ├── JwtUtil.java              ← JWT Token 生成/解析工具
│   │   │   ├── CorsConfig.java           ← CORS 跨域配置
│   │   │   ├── WebMvcConfig.java         ← 拦截器注册
│   │   │   └── DatabaseInitializer.java  ← 数据库初始化（建表+默认数据）
│   │   ├── controller/
│   │   │   ├── AuthController.java       ← 登录/注册 API
│   │   │   ├── ProductController.java    ← 商品 API
│   │   │   ├── CartController.java       ← 购物车 API
│   │   │   └── OrderController.java      ← 订单 API
│   │   ├── service/                      ← 业务逻辑层（接口+实现分离）
│   │   ├── domain/                       ← 实体类
│   │   ├── dto/                          ← 数据传输对象
│   │   ├── repository/                   ← MyBatis Mapper 接口
│   │   ├── interceptor/
│   │   │   ├── JwtAuthInterceptor.java   ← JWT 认证拦截器
│   │   │   └── RequestLogInterceptor.java ← 请求日志拦截器
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java ← 全局异常处理
│   └── pom.xml
│
├── frontend/                             ← Vue 3 前端
│   ├── src/
│   │   ├── api/                          ← API 接口封装（auth/product/cart/order）
│   │   ├── router/index.js               ← 路由 + 导航守卫（Token 校验）
│   │   ├── utils/
│   │   │   ├── request.js                ← Axios 封装（请求/响应拦截器）
│   │   │   └── auth.js                   ← Token 存储/读取工具
│   │   └── views/                        ← 页面组件
│   │       ├── Login.vue                 ← 登录页
│   │       ├── Register.vue              ← 注册页
│   │       ├── Home.vue                  ← 首页（商品列表）
│   │       ├── ProductDetail.vue         ← 商品详情
│   │       ├── Cart.vue                  ← 购物车
│   │       ├── Checkout.vue              ← 结算/下单
│   │       ├── OrderList.vue             ← 订单列表
│   │       ├── OrderDetail.vue           ← 订单详情
│   │       ├── Profile.vue               ← 个人中心
│   │       └── Layout.vue                ← 公共布局
│   ├── package.json
│   └── vite.config.js
│
├── .gitignore
└── README.md
```

---

## 🔐 JWT 登录验证原理

```
┌──────────┐     POST /api/auth/login     ┌──────────────┐
│  前端     │  ── {username, password} ──→ │   Spring Boot │
│ (Vue 3)  │                              │   后端         │
│          │  ←── {token, userId, ...} ── │              │
│ localStorage.setItem('token', token)     │  1. 验证密码   │
│          │                               │  2. SHA-256 比对│
│          │  GET /api/orders              │  3. JWT 签名生成│
│          │  ── Authorization: Bearer ──→ │              │
│          │     <token>                   │  4. 解析 Token │
│          │                               │  5. 提取 userId│
│          │  ←── {code:200, data:[...]} ─ │  6. 执行业务   │
└──────────┘                               └──────────────┘
```

**关键步骤：**

1. **登录** → 前端发送 `{username, password}`，后端验证后调用 `JwtUtil.generateToken()` 生成 JWT
2. **存储** → 前端将 Token 存入 `localStorage`
3. **请求拦截** → Axios 请求拦截器自动在 `Authorization: Bearer <token>` 头附加 Token
4. **后端验证** → `JwtAuthInterceptor.preHandle()` 解析 Token，验证签名和有效期
5. **注入用户** → 验证通过后将 `userId` 和 `username` 存入 `request` 属性
6. **Token 过期** → 后端返回 `401`，前端 Axios 响应拦截器自动清除 Token 并跳转登录页
7. **路由守卫** → Vue Router `beforeEach` 检查 `isAuthenticated()`，未登录只能访问 Login/Register

---

## 📡 API 接口

### 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `POST` | `/api/auth/login` | 用户登录 | 否 |
| `POST` | `/api/auth/register` | 用户注册 | 否 |
| `GET` | `/api/auth/me` | 获取当前用户信息 | 是 |

### 商品接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `GET` | `/api/products` | 商品列表 | 否 |
| `GET` | `/api/products/search?keyword=` | 搜索商品 | 否 |
| `GET` | `/api/products/{id}` | 商品详情 | 否 |
| `GET` | `/api/products/category/{category}` | 按分类筛选 | 否 |

### 购物车接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `GET` | `/api/cart` | 获取购物车列表 | 是 |
| `POST` | `/api/cart` | 添加到购物车 | 是 |
| `PUT` | `/api/cart/{id}` | 修改数量 | 是 |
| `DELETE` | `/api/cart/{id}` | 删除商品 | 是 |
| `DELETE` | `/api/cart` | 清空购物车 | 是 |

### 订单接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `POST` | `/api/orders` | 创建订单（下单） | 是 |
| `GET` | `/api/orders` | 查询当前用户订单 | 是 |
| `GET` | `/api/orders/{id}` | 订单详情 | 是 |
| `PUT` | `/api/orders/{id}/cancel` | 取消订单 | 是 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

---

## 📦 订单查询前后端交互过程

```
用户点击「我的订单」
    │
    ▼
Vue Router 导航到 /orders（路由守卫检查 Token）
    │
    ▼
OrderList.vue onMounted() → orderApi.list()
    │
    ▼
Axios 发起 GET /api/orders（请求拦截器自动附加 Authorization 头）
    │
    ▼
JwtAuthInterceptor.preHandle()
  ├── 提取 Bearer Token
  ├── JwtUtil.parseToken() 验证签名 + 有效期
  ├── 解析出 userId
  └── request.setAttribute("userId", userId)
    │
    ▼
OrderController.listOrders() 获取 userId
    │
    ▼
OrderService.getOrdersByUserId(userId)
    │
    ▼
OrderMapper.findByUserId() 执行 SQL
  → SELECT o.*, u.username FROM orders o
    LEFT JOIN user u ON o.user_id = u.id
    WHERE o.user_id = #{userId}
    ORDER BY o.create_time DESC
  → 同时映射 order_item（通过 collection 标签实现 1:N 查询）
    │
    ▼
返回 Result<List<Order>>（每个 Order 含 items 列表）
    │
    ▼
前端 OrderList.vue 接收 res.data
  → el-table 渲染订单列表
  → 显示订单号、状态（带颜色标签）、商品明细、金额
  → 支持点击进入详情、取消待支付订单
```

---

## 🗄️ 数据库表结构

```
user ──┬── cart_item       (用户 → 购物车：一对多)
       │
       └── orders ──┬── order_item    (订单 → 订单明细：一对多)
                     │
product ─────────────┘                (商品引用)
```

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `user` | 用户表 | id, username, password(SHA-256), phone, email, address, role |
| `product` | 商品表 | id, name, description, price, stock, category, image, status |
| `cart_item` | 购物车 | id, user_id, product_id, quantity |
| `orders` | 订单表 | id, order_no, user_id, total_amount, status, address, remark |
| `order_item` | 订单明细 | id, order_id, product_id, product_name, quantity, price |

---

## ⚙️ 数据库切换

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  profiles:
    active: sqlite   # 默认，零配置
    # active: mysql  # 切换到 MySQL（需先创建数据库 ecommerce）
```

---

## 🧪 测试

```bash
# 后端测试
cd backend
mvn test

# 前端构建
cd frontend
npm run build
```

---

## 📝 开发规范

- **分层架构**：Controller → Service → Repository → Mapper（四层分离）
- **接口与实现分离**：Service 接口放在 `service` 包，实现放在 `service.impl` 包
- **拦截器独立**：认证拦截器和日志拦截器放在 `interceptor` 包
- **统一响应**：所有 API 使用 `Result<T>` 封装
- **统一异常**：`GlobalExceptionHandler` 全局捕获处理
- **前端 API 集中管理**：`src/api/` 目录按模块组织
- **Token 统一管理**：`utils/auth.js` + Axios 拦截器

---

## 📜 License

MIT License

---

> 🚀 由 Claude (Opus 4.8) 辅助生成，遵循工程化规范。前后端完整交互，Token 认证闭环。
