package com.demo.PaymentService.util;

import com.demo.PaymentService.dto.GeneralResponse;

public class GeneralResponseConvert {

//    public static GeneralResponse forOrderResponse(String msg, Long status, String statusDesc){
//        return GeneralResponse.builder()
//                .response(new OrderResponse(msg))
//                .status(status)
//                .statusDesc(statusDesc)
//                .build();
//    }

    public static GeneralResponse forException(String message) {
        return GeneralResponse.builder()
                .response(null)
                .status(-1L)
                .statusDesc(message)
                .build();
    }
}
