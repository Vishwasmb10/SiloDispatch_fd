package com.example.SiloDispatch.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class OrderCsvDto {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String address;
    private String pincode;
    private BigDecimal distanceKm;
    private BigDecimal amount;
    private BigDecimal weightKg;
    private String paymentType;
}