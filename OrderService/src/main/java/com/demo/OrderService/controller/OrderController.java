package com.demo.OrderService.controller;

import com.demo.OrderService.service.OrderService;
import com.demo.OrderService.util.PasswordEncoder;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/api")
public class OrderController {

    @Autowired
    OrderService service;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/checkOrder")
    public ResponseEntity<?> checkOrder(){
        return service.checkOrder();
    }

    @GetMapping("/cart")
    public ResponseEntity<?> checkCart(){
        return service.checkCart();
    }



}
