package com.demo.CartService.repository;

import com.demo.CartService.entity.ProductDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductDetailsEntity, Long> {
}

