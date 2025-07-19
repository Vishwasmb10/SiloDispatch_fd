package com.example.SiloDispatch.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverCashInfo {
    private Long driverId;
    private String driverName;
    private BigDecimal cashInHand;
}

