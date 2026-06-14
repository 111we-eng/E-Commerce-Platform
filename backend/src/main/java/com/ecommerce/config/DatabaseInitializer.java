package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * 数据库初始化：建表 + 插入默认用户和示例商品
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // ===== 用户表 =====
            stmt.execute("CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(128) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "email VARCHAR(100), " +
                    "address VARCHAR(255), " +
                    "role VARCHAR(20) DEFAULT 'USER', " +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // ===== 商品表 =====
            stmt.execute("CREATE TABLE IF NOT EXISTS product (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "description TEXT, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "stock INTEGER DEFAULT 0, " +
                    "category VARCHAR(50), " +
                    "image VARCHAR(500), " +
                    "status VARCHAR(20) DEFAULT 'ON', " +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // ===== 购物车表 =====
            stmt.execute("CREATE TABLE IF NOT EXISTS cart_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL DEFAULT 1, " +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES user(id), " +
                    "FOREIGN KEY (product_id) REFERENCES product(id))");

            // ===== 订单表 =====
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_no VARCHAR(32) NOT NULL UNIQUE, " +
                    "user_id INTEGER NOT NULL, " +
                    "total_amount DECIMAL(10,2) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "address VARCHAR(255), " +
                    "remark VARCHAR(500), " +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES user(id))");

            // ===== 订单明细表 =====
            stmt.execute("CREATE TABLE IF NOT EXISTS order_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "product_name VARCHAR(200), " +
                    "quantity INTEGER NOT NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "FOREIGN KEY (order_id) REFERENCES orders(id), " +
                    "FOREIGN KEY (product_id) REFERENCES product(id))");

            System.out.println("[DB] 数据库表结构初始化完成");

            // ===== 插入默认管理员 =====
            String encryptedPwd = sha256("admin123");
            String upsertUser = "INSERT OR IGNORE INTO user (id, username, password, phone, email, address, role) " +
                    "VALUES (1, 'admin', ?, '13800138000', 'admin@ecommerce.com', '北京市朝阳区', 'ADMIN')";
            try (PreparedStatement ps = conn.prepareStatement(upsertUser)) {
                ps.setString(1, encryptedPwd);
                ps.executeUpdate();
            }

            // 插入普通测试用户
            String upsertUser2 = "INSERT OR IGNORE INTO user (id, username, password, phone, email, address, role) " +
                    "VALUES (2, 'user', ?, '13900139000', 'user@ecommerce.com', '上海市浦东新区', 'USER')";
            try (PreparedStatement ps = conn.prepareStatement(upsertUser2)) {
                ps.setString(1, encryptedPwd);
                ps.executeUpdate();
            }

            System.out.println("[DB] 默认用户初始化完成");

            // ===== 插入示例商品（仅首次）=====
            String countSql = "SELECT COUNT(*) FROM product";
            int count = 0;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                var rs = ps.executeQuery();
                if (rs.next()) count = rs.getInt(1);
            }

            if (count == 0) {
                insertSampleProducts(conn);
                System.out.println("[DB] 示例商品初始化完成 (" + getProductCount(conn) + " 件)");
            }

        } catch (Exception e) {
            System.err.println("[DB] 数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertSampleProducts(Connection conn) throws Exception {
        String sql = "INSERT INTO product (name, description, price, stock, category, image, status) VALUES (?, ?, ?, ?, ?, ?, 'ON')";
        Object[][] products = {
                {"MacBook Pro 14寸", "Apple M3 Pro芯片 / 18GB内存 / 512GB固态硬盘 / Liquid Retina XDR显示屏", 14999.00, 50, "电子产品", "https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01wRlRqP1mVqNwXW3ZL_!!2200721531555.jpg"},
                {"iPhone 16 Pro", "A18 Pro芯片 / 256GB存储 / 钛金属设计 / 4800万像素主摄", 8999.00, 100, "电子产品", "https://img.alicdn.com/imgextra/i4/2211765384132/O1CN01kPlWPW1rMCEvJXg3H_!!2211765384132.jpg"},
                {"AirPods Pro 3", "自适应降噪 / 空间音频 / 支持USB-C充电 / 6小时续航", 1899.00, 200, "电子产品", "https://img.alicdn.com/imgextra/i2/2207957235079/O1CN01UxKXrJ2GRBKoBPwlU_!!2207957235079.jpg"},
                {"Nike Air Max 270", "经典气垫跑鞋 / 透气网面 / 耐磨橡胶底 / 经典黑白配色", 899.00, 80, "运动鞋服", "https://img.alicdn.com/imgextra/i3/2219470023707/O1CN01sHDqvC1eWBNv6H4W6_!!2219470023707.jpg"},
                {"华为 MatePad Pro", "12.6寸OLED屏 / 麒麟9000S / 12GB+256GB / 支持M-Pencil", 4299.00, 60, "电子产品", "https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01w4sRNW1mVqNumBx33_!!2200721531555.jpg"},
                {"索尼 WH-1000XM6", "行业标杆降噪 / 30小时续航 / Hi-Res Audio / 轻量化设计", 2499.00, 120, "电子产品", "https://img.alicdn.com/imgextra/i1/2200721531555/O1CN01fLVXMp1mVqNtjJRY2_!!2200721531555.jpg"},
                {"Adidas Ultraboost", "爆米花缓震跑鞋 / Primeknit针织鞋面 / Continental橡胶外底", 1099.00, 70, "运动鞋服", "https://img.alicdn.com/imgextra/i2/2219470023707/O1CN01PI28GQ1eWBNwwJg0A_!!2219470023707.jpg"},
                {"戴森 V16 吸尘器", "数字马达 / 激光探测微尘 / 60分钟续航 / 整机HEPA过滤", 3999.00, 40, "生活家电", "https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01bUKVUJ1mVqNx6JJ0z_!!2200721531555.jpg"},
        };

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] p : products) {
                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setDouble(3, (Double) p[2]);
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setString(6, (String) p[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private int getProductCount(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM product");
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 加密失败", e);
        }
    }
}
