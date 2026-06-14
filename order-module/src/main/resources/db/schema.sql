-- ============================================
-- 电商订单模块 — 数据库表结构
-- 数据库：MySQL 5.7+ / H2
-- ============================================

-- 订单主表
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no         VARCHAR(32)   NOT NULL UNIQUE COMMENT '订单编号',
    user_id          BIGINT        NOT NULL COMMENT '用户ID',
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    receiver_name    VARCHAR(50)   COMMENT '收货人姓名',
    receiver_phone   VARCHAR(20)   COMMENT '收货人电话',
    receiver_address VARCHAR(255)  COMMENT '收货地址',
    remark           VARCHAR(500)  COMMENT '订单备注',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '订单主表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id        BIGINT        NOT NULL COMMENT '订单ID',
    product_id      BIGINT        NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(200)  COMMENT '商品名称',
    product_price   DECIMAL(10,2) COMMENT '商品单价',
    quantity        INT           NOT NULL DEFAULT 1 COMMENT '购买数量',
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) COMMENT '订单明细表';

-- 插入测试数据
INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, receiver_phone, receiver_address)
SELECT * FROM (
    VALUES
        ('ORD202601010001', 1, 2999.00, 'PENDING',  '张三', '13800000001', '北京市朝阳区望京SOHO T1 12层'),
        ('ORD202601010002', 1, 1899.50, 'PAID',     '张三', '13800000001', '北京市朝阳区望京SOHO T1 12层'),
        ('ORD202601020001', 2, 5698.00, 'SHIPPED',  '李四', '13800000002', '上海市浦东新区陆家嘴金融中心 8楼'),
        ('ORD202601030001', 3, 156.80,  'COMPLETED','王五', '13800000003', '广州市天河区珠江新城 15号'),
        ('ORD202601030002', 1, 4400.00, 'CANCELLED','张三', '13800000001', '北京市朝阳区望京SOHO T1 12层')
) AS t (order_no, user_id, total_amount, status, receiver_name, receiver_phone, receiver_address)
WHERE NOT EXISTS (SELECT 1 FROM orders LIMIT 1);

-- 插入测试明细数据
INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity)
SELECT * FROM (
    VALUES
        (1, 101, 'MacBook Pro 14寸', 14999.00, 1),
        (1, 102, 'AirPods Pro', 1899.00, 1),
        (2, 103, 'Nike Air Max 270', 899.00, 2),
        (2, 104, 'Adidas 运动袜(3双装)', 101.50, 1),
        (3, 105, '华为 MatePad Pro', 4299.00, 1),
        (3, 106, '华为 M-Pencil', 699.00, 1),
        (3, 107, '平板保护套', 700.00, 1),
        (4, 108, '小米电动牙刷', 156.80, 1),
        (5, 101, 'MacBook Pro 14寸', 14999.00, 2)
) AS t (order_id, product_id, product_name, product_price, quantity)
WHERE NOT EXISTS (SELECT 1 FROM order_items LIMIT 1);
