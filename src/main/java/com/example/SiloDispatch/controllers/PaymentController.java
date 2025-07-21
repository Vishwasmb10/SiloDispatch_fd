package com.example.SiloDispatch.controllers;

import com.example.SiloDispatch.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<?> initiatePayment(@PathVariable Long orderId) {
        String razorOrder = paymentService.initiatePayment(orderId);
        return ResponseEntity.ok(razorOrder);
    }

    @PostMapping("/success")
    public ResponseEntity<?> markSuccess(@RequestBody Map<String, String> payload) {
        String razorOrderId = payload.get("razorpay_order_id");
        paymentService.markPaymentSuccess(razorOrderId);
        return ResponseEntity.ok("Payment marked successful");
    }

    @PostMapping("/cash")
    public ResponseEntity<?> receiveCash(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        BigDecimal amount=paymentService.receiveCash(orderId);
        return ResponseEntity.ok(amount);
    }

}

