package com.ecommerce.controller;

import com.ecommerce.domain.Product;
import com.ecommerce.dto.Result;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取所有上架商品（无需登录）
     */
    @GetMapping
    public Result<List<Product>> list() {
        return Result.ok(productService.getAllProducts());
    }

    /**
     * 搜索商品（无需登录）
     */
    @GetMapping("/search")
    public Result<List<Product>> search(@RequestParam String keyword) {
        return Result.ok(productService.searchProducts(keyword));
    }

    /**
     * 商品详情（无需登录）
     */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.getProductById(id));
    }

    /**
     * 按分类获取商品（无需登录）
     */
    @GetMapping("/category/{category}")
    public Result<List<Product>> byCategory(@PathVariable String category) {
        return Result.ok(productService.getByCategory(category));
    }
}
