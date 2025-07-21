package com.example.SiloDispatch.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderForBatching {
    private Long orderId;
    private BigDecimal lat;
    private BigDecimal lon;
    private BigDecimal weightKg;
    private String pincode;
}
