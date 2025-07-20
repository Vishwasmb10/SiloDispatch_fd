package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.Dto.BatchWithOrdersDTO;
import com.example.SiloDispatch.Dto.DriverDto;
import com.example.SiloDispatch.services.DriverViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverViewService driverViewService;

    @GetMapping("/available")
    public List<DriverDto> getAvailableDrivers() {
        return driverViewService.getAvailableDrivers(); // implement this method
    }

    @GetMapping("/{driverId}/orders")
    public List<BatchWithOrdersDTO> getOrdersForDriver(@PathVariable Long driverId) {
        return driverViewService.getOrdersGroupedByBatch(driverId);
    }
}
