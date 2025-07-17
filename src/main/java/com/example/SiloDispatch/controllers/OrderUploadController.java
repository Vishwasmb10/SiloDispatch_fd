package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.Dto.OrderCsvDto;
import com.example.SiloDispatch.services.OrderUploadService;
import com.example.SiloDispatch.util.CsvHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderUploadController {

    private final OrderUploadService orderUploadService;

    @GetMapping("/home")
    public String home() {
        return "Home...";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ File is empty");
            }

            List<OrderCsvDto> orders = CsvHelper.csvToOrders(file);
            orderUploadService.processCsvOrders(orders);

            return ResponseEntity.ok("✅ CSV processed and orders saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Error processing CSV: " + e.getMessage());
        }
    }
}
