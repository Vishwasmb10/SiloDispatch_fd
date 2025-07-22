package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.batch.OrderBatchProcessor;
import com.example.SiloDispatch.services.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batching")
@RequiredArgsConstructor
public class BatchingController {

    private final BatchService batchService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateBatches() {
        batchService.generateBatches();
        return ResponseEntity.ok("Batches generated successfully");
    }

    @PostMapping("/complete/{driverId}")
    public String markBatchAsCompleted(@PathVariable Long driverId) {
        boolean success = batchService.markBatchAsCompleted(driverId);
        return success ? "Batch marked as completed." : "No active batch found for the driver.";
    }
}

