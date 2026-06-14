package com.ecommerce.service;

import com.ecommerce.domain.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getByCategory(String category);
    Product getProductById(Long id);
    List<Product> searchProducts(String keyword);
}
