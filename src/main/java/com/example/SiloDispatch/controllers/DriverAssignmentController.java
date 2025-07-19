package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.services.DriverAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assign")
@RequiredArgsConstructor
public class DriverAssignmentController {

    private final DriverAssignmentService assignmentService;

    @PostMapping("/batches")
    public String assignBatchesToDrivers(@RequestBody List<Long> availableDriverIds) {
        assignmentService.assignBatches(availableDriverIds);
        return "Batches assigned successfully";
    }
}
