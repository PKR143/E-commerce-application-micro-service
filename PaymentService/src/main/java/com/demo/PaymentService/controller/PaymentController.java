package com.demo.PaymentService.controller;

import com.demo.PaymentService.dto.CardRequest;
import com.demo.PaymentService.dto.OtpRequest;
import com.demo.PaymentService.dto.TxnRequest;
import com.demo.PaymentService.exception.EcommerceException;
import com.demo.PaymentService.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PaymentService service;

    @PostMapping("/doPayment")
    public ResponseEntity<?> doPayment(@RequestBody @Valid TxnRequest request)throws RuntimeException {
        return service.doPayment(request);
    }

    @PostMapping("/completePayment")
    public ResponseEntity<?> completePayment(@RequestBody OtpRequest request){
        return service.completeTxn(request.getTxnId(), request.getOtp());
    }

    @PostMapping("/addCard")
    public ResponseEntity<?> addCard(@RequestBody @Valid CardRequest card)throws EcommerceException {
        card.setPin(passwordEncoder.encode(card.getPin()));
        return service.addCard(card);
    }
}
