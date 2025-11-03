package com.demo.OrderService.util;

import com.demo.OrderService.exception.EcommerceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;

@Component
@Slf4j
public class PasswordEncoder {


    public String encode(String password){
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        }catch(Exception e){
            log.warn("Exception while Encrypting the password Due to: {}",e.getMessage());
            throw new EcommerceException("Exception while Encrypting the password!");
        }
    }

}
