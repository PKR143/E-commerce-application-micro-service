package com.demo.ProductService.repository;

import com.demo.ProductService.entity.ProductDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductDetailsEntity, Long> {
}
