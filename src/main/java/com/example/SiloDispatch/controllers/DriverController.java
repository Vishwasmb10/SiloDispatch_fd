package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.Dto.BatchWithOrdersDTO;
import com.example.SiloDispatch.Dto.DriverDto;
import com.example.SiloDispatch.services.DriverViewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public List<BatchWithOrdersDTO> getOrdersForDriver(@PathVariable Long driverId,HttpSession session) {
        Long sessionDriverId = (Long) session.getAttribute("driverId");
        if (!driverId.equals(sessionDriverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return driverViewService.getOrdersGroupedByBatch(driverId);

    }

    @GetMapping("/id")
    public ResponseEntity<Long> getDriverId(HttpSession session) {
        Long driverId = (Long) session.getAttribute("driverId");
        if (driverId != null) {
            return ResponseEntity.ok(driverId);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
