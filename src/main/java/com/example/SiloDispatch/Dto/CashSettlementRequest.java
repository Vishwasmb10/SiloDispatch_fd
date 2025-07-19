package com.example.SiloDispatch.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashSettlementRequest {
    private Long driverId;
    private BigDecimal collectedAmount;
}

