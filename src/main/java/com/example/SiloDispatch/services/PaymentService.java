package com.example.SiloDispatch.services;

import com.example.SiloDispatch.models.CashLedger;
import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.models.Payment;
import com.example.SiloDispatch.repositories.CashLedgerRepository;
import com.example.SiloDispatch.repositories.DriverRepository;
import com.example.SiloDispatch.repositories.OrderRepository;
import com.example.SiloDispatch.repositories.PaymentRepository;
import com.example.SiloDispatch.util.RazorpayClientHolder;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final CashLedgerRepository cashLedgerRepo;
    private final DriverRepository driverRepo;

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

        Order order = orderRepo.findByOrderId(payment.getOrderId());
        // For postpaid orders (but not COD), mark as delivered
        orderRepo.updateDeliveryStatus(order.getId(), Order.DeliveryStatus.DELIVERED);
    }

    @Transactional
    public void receiveCash(Long orderId) {
        // 1. Get the order
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Update payments table
        Payment payment = paymentRepo.findByTransactionId(orderId.toString()).orElse(null);
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(orderId);
        }
        payment.setStatus(Order.PaymentStatus.SUCCESS);
        payment.setTransactionId(orderId.toString());
        payment.setMethod(Order.PaymentType.COD);
        paymentRepo.save(payment);

        // 3. Update orders table
        order.setPaymentStatus(Order.PaymentStatus.SUCCESS);
        order.setPaymentType(Order.PaymentType.COD);
        order.setDeliveryStatus(Order.DeliveryStatus.DELIVERED);
        orderRepo.save(order);

        // 4. Add to cash ledger
        CashLedger ledger = new CashLedger();
        ledger.setOrderId(orderId);
        ledger.setDriverId(order.getDriverId());
        ledger.setAmount(order.getAmount());
        ledger.setType(CashLedger.LedgerType.COLLECT);
        cashLedgerRepo.save(ledger);

        // 5. Add to driver's cash_in_hand
        driverRepo.incrementCashInHand(order.getDriverId(), order.getAmount());
    }

}

