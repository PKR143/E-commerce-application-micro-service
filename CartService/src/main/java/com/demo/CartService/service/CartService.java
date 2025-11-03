package com.demo.CartService.service;

import com.demo.CartService.dto.CartRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface CartService {
    ResponseEntity<?> addToCart(@RequestBody CartRequest request);
    ResponseEntity<?> getCartItems(@PathVariable String username);
}
