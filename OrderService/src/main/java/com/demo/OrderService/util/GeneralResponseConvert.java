package com.demo.OrderService.util;

import com.demo.OrderService.dto.GeneralResponse;
import com.demo.OrderService.dto.OrderResponse;

public class GeneralResponseConvert {
    public static GeneralResponse forOrderResponse(String msg, Long status, String statusDesc){
        return GeneralResponse.builder()
                .response(new OrderResponse(msg))
                .status(status)
                .statusDesc(statusDesc)
                .build();
    }

    public static GeneralResponse forException(String message) {
        return GeneralResponse.builder()
                .response(null)
                .status(-1L)
                .statusDesc(message)
                .build();
    }
}
