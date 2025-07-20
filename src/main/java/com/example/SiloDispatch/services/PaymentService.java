package com.example.SiloDispatch.services;

import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.models.Payment;
import com.example.SiloDispatch.repositories.OrderRepository;
import com.example.SiloDispatch.repositories.PaymentRepository;
import com.example.SiloDispatch.util.RazorpayClientHolder;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    public String initiatePayment(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order"));

        try {
            RazorpayClient client = RazorpayClientHolder.getClient();
            JSONObject options = new JSONObject();
            options.put("amount", order.getAmount().multiply(BigDecimal.valueOf(100))); // INR to paise
            options.put("currency", "INR");
            options.put("receipt", "order_rcpt_" + orderId);

            com.razorpay.Order razorOrder = client.orders.create(options);

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(order.getAmount());
            payment.setMethod(Order.PaymentType.PREPAID);
            payment.setTransactionId(razorOrder.get("id"));
            payment.setStatus(Order.PaymentStatus.PENDING);
            paymentRepo.save(payment);

            return razorOrder.toString();
        } catch (RazorpayException e) {
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    public void markPaymentSuccess(String razorpayOrderId) {
        Payment payment = paymentRepo.findByTransactionId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        payment.setStatus(Order.PaymentStatus.SUCCESS);
        paymentRepo.save(payment);

        orderRepo.updatePaymentStatus(payment.getOrderId(), Order.PaymentStatus.SUCCESS);
    }
}

