package com.demo.OrderService.exception;

import com.demo.OrderService.dto.GeneralResponse;
import com.demo.OrderService.util.GeneralResponseConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EcommerceException.class)
    public ResponseEntity<?> handleEcommerceException(EcommerceException e){
      log.warn("Handling Ecommerce Exception.");
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GeneralResponseConvert.forException(e.getMessage()));
    }

}
