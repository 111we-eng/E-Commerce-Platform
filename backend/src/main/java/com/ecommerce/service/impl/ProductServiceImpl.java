package com.ecommerce.service.impl;

import com.ecommerce.domain.Product;
import com.ecommerce.repository.ProductMapper;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    @Override
    public List<Product> getByCategory(String category) {
        return productMapper.findByCategory(category);
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productMapper.search(keyword);
    }
}
