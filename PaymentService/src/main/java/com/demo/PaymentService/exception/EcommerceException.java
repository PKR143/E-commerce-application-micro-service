package com.demo.PaymentService.exception;

public class EcommerceException extends RuntimeException{
    private String message;
    public EcommerceException(String msg){
        this.message = msg;
    }
}
