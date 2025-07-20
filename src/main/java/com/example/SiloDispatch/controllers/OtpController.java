package com.example.SiloDispatch.controllers;


import com.example.SiloDispatch.services.OtpService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    // Endpoint to send OTP
    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestBody OtpRequest request) {
        try {
            String otp = otpService.generateOtp(request.getOrderId(), request.getPhoneNumber());
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send OTP: " + e.getMessage());
        }
    }

    // Endpoint to verify OTP
    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean valid = otpService.verifyOtp(request.getOrderId(), request.getOtp());

        if (valid) {
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @Data
    public static class OtpRequest {
        private Long orderId;
        private String phoneNumber;
    }

    @Data
    public static class OtpVerifyRequest {
        private Long orderId;
        private String otp;
    }
}
