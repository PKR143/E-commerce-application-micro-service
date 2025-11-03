package com.demo.PaymentService.service;

import com.demo.PaymentService.dto.CardRequest;
import com.demo.PaymentService.dto.TxnRequest;
import com.demo.PaymentService.exception.EcommerceException;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    ResponseEntity<?> doPayment(TxnRequest request)throws RuntimeException;
    ResponseEntity<?> completeTxn(Long txnId, String otp);
    ResponseEntity<?> addCard(CardRequest card)throws EcommerceException;
}
