package com.ecommerce.repository;

import com.ecommerce.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findAll();
    List<Product> findByCategory(@Param("category") String category);
    Product findById(@Param("id") Long id);
    List<Product> search(@Param("keyword") String keyword);
    int insert(Product product);
    int update(Product product);
    int deleteById(@Param("id") Long id);
}
