package com.example.SiloDispatch.services;

import com.example.SiloDispatch.Dto.DriverCashInfo;
import com.example.SiloDispatch.models.Driver;
import com.example.SiloDispatch.repositories.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverCashService {

    private final DriverRepository driverRepository;

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

        BigDecimal newBalance = driver.getCashInHand().subtract(collectedAmount);
        driver.setCashInHand(newBalance);
        driverRepository.save(driver);

        return newBalance;
    }

}

