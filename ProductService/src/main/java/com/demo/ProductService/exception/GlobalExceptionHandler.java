package com.demo.ProductService.exception;

import com.demo.ProductService.dto.GeneralResponse;
import com.demo.ProductService.entity.EcommerceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EcommerceException.class)
    public ResponseEntity<?> handleOrderException(EcommerceException e){
        log.info("handling EcommerceException");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse(null, -1L, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.info("handling MethodArgumentNotValidException");
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error-> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse(null,-1L,errors.values().toString()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        log.info("handling HttpMessageNotReadableException");
        e.printStackTrace();
        String[] error = e.getMessage().split(":");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse(null, -1L,error[0]));
    }

//    @ExceptionHandler(ExpiredJwtException.class)
//    public ResponseEntity<?> handleExpiredJwt(ExpiredJwtException ex) {
//        log.info("handling ExpiredJwtException");
//        return ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .body(new GeneralResponse(null, -1L, "Jwt Token expired"));
//    }
//    @ExceptionHandler(JwtException.class)
//    public ResponseEntity<?> handleExpiredJwt(JwtException ex) {
//        log.info("handling JwtException");
//        return ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .body(new GeneralResponse(null, -1L, "invalid Jwt Token provided"));
//    }
//    @ExceptionHandler(SignatureException.class)
//    public ResponseEntity<Map<String,String>> handleSignatureException(SignatureException ex) {
//        log.info("handling SignatureException");
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(Map.of("error","Invalid JWT signature"));
//    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException e){
        log.info("handling IO Exception");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new GeneralResponse(null, -1L, "Something went wrong, please try again!"));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> Exception(Exception e){
        log.info("Exception @{} due to: {}",e.getClass(),e.getMessage());
        return ResponseEntity.badRequest().body(new GeneralResponse(null,-1L,e.getMessage()));
    }
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<?> RuntimeException(RuntimeException e){
//        log.info("RuntimeException @{} due to: {}",e.getClass(),e.getMessage());
//        return ResponseEntity.badRequest().body(new GeneralResponse(null,-1L,e.getMessage()));
//    }
}

