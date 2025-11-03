package com.demo.ProductService.service;

import com.demo.ProductService.dto.ProductRequest;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    ResponseEntity<?> createProduct(ProductRequest request);
    ResponseEntity<?> getProductById(Long id);
    ResponseEntity<?> getAllProducts();
    ResponseEntity<?> updateProduct(Long id, ProductRequest request);
    ResponseEntity<?> deleteProduct(Long id);
}
