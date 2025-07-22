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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;

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
            payment.setCreatedAt(LocalDateTime.now());
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
    public BigDecimal receiveCash(Long orderId) {
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
        payment.setAmount(order.getAmount());
        System.out.println(payment.getTransactionId());
        payment.setCreatedAt(LocalDateTime.now());
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
        return order.getAmount();
    }

    public boolean verifyAndHandleWebhook(String signature, String payload) {
        try {
            String secret = System.getenv("RAZORPAY_WEBHOOK_SECRET"); // Set this as an env variable

            // Compute expected signature
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes());
            String expectedSignature = Base64.getEncoder().encodeToString(hash);

            if (!expectedSignature.equals(signature)) {
                System.out.println("❌ Invalid signature");
                return false;
            }

            // Valid signature → parse payload
            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");

            if ("payment.captured".equals(event)) {
                JSONObject paymentObj = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String razorpayOrderId = paymentObj.getString("order_id");
                String razorpayPaymentId = paymentObj.getString("id");

                System.out.println("✅ Captured: " + razorpayPaymentId + " for order: " + razorpayOrderId);
                markPaymentSuccess(razorpayOrderId);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

