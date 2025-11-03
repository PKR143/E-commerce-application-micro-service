package com.demo.PaymentService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralResponse {

    private Response response;
    private Long status;
    private String statusDesc;
}
