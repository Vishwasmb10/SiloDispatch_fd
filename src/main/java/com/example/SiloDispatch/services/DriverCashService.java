package com.example.SiloDispatch.services;

import com.example.SiloDispatch.Dto.DriverCashInfo;
import com.example.SiloDispatch.models.CashLedger;
import com.example.SiloDispatch.models.Driver;
import com.example.SiloDispatch.repositories.CashLedgerRepository;
import com.example.SiloDispatch.repositories.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverCashService {

    private final DriverRepository driverRepository;
    private final CashLedgerRepository cashLedgerRepository;

    public List<DriverCashInfo> getAllDriverCashInHand() {
        return driverRepository.findAll().stream()
                .map(driver -> new DriverCashInfo(driver.getId(), driver.getName(), driver.getCashInHand()))
                .toList();
    }

    public BigDecimal settleCash(Long driverId, BigDecimal collectedAmount) {
        Optional<Driver> optionalDriver = driverRepository.findById(driverId);
        if (optionalDriver.isEmpty()) {
            throw new IllegalArgumentException("Driver not found");
        }

        Driver driver = optionalDriver.get();

        if (driver.getCashInHand().compareTo(collectedAmount) < 0) {
            throw new IllegalArgumentException("Collected amount exceeds current cash in hand");
        }

        // Update driver's cash in hand
        BigDecimal newBalance = driver.getCashInHand().subtract(collectedAmount);
        driver.setCashInHand(newBalance);
        driverRepository.save(driver);

        // Log in ledger
        CashLedger ledgerEntry = new CashLedger();
        ledgerEntry.setDriverId(driverId);
        ledgerEntry.setOrderId(null); // No specific order associated
        ledgerEntry.setAmount(collectedAmount);
        ledgerEntry.setType(CashLedger.LedgerType.SETTLE);
        ledgerEntry.setTimestamp(LocalDateTime.now());

        cashLedgerRepository.save(ledgerEntry);

        return newBalance;
    }
}
