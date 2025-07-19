package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.Dto.CashSettlementRequest;
import com.example.SiloDispatch.Dto.DriverCashInfo;
import com.example.SiloDispatch.services.DriverCashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverCashController {

    private final DriverCashService driverCashService;

    @GetMapping("/cash-in-hand")
    public List<DriverCashInfo> getCashInHandForAllDrivers() {
        return driverCashService.getAllDriverCashInHand();
    }

    @PostMapping("/settle-cash")
    public ResponseEntity<String> settleCash(@RequestBody CashSettlementRequest request) {
        try {
            BigDecimal updatedBalance = driverCashService.settleCash(request.getDriverId(), request.getCollectedAmount());
            return ResponseEntity.ok("Cash settlement successful. Updated balance: ₹" + updatedBalance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
