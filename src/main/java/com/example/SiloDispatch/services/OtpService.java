package com.example.SiloDispatch.services;

import com.example.SiloDispatch.config.TwilioConfig;
import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.models.OtpVerification;
import com.example.SiloDispatch.repositories.OrderRepository;
import com.example.SiloDispatch.repositories.OtpVerificationRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepo;
    private final TwilioConfig twilioConfig;
    private final OrderRepository orderRepo; // Inject this via constructor


    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 1;

    public String generateOtp(Long orderId, String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        try {
            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioConfig.getFromPhoneNumber()),
                    "Your delivery OTP is: " + otp
            ).create();

            // Save to DB only after successful Twilio send
            OtpVerification verification = new OtpVerification();
            verification.setOrderId(orderId);
            verification.setOtp(otp);
            verification.setSentAt(LocalDateTime.now());
            verification.setStatus(OtpVerification.OtpStatus.SENT);
            otpRepo.save(verification);

            orderRepo.updateOtpStatus(orderId, OtpVerification.OtpStatus.SENT);

            return otp;

        } catch (Exception e) {
            throw new RuntimeException("Twilio failed to send OTP: " + e.getMessage());
        }
    }

    public boolean verifyOtp(Long orderId, String enteredOtp) {
        Optional<OtpVerification> optional = otpRepo.findTopByOrderIdAndStatusOrderBySentAtDesc(orderId, OtpVerification.OtpStatus.SENT);

        if (optional.isEmpty()) return false;

        OtpVerification record = optional.get();

        // Expiry Check
        if (Duration.between(record.getSentAt(), LocalDateTime.now()).toMinutes() >= EXPIRY_MINUTES) {
            record.setStatus(OtpVerification.OtpStatus.EXPIRED);
            otpRepo.save(record);
            orderRepo.updateOtpStatus(orderId, OtpVerification.OtpStatus.EXPIRED);
            return false;
        }

        if (!record.getOtp().equals(enteredOtp)) {
            return false;
        }

        // Valid OTP
        record.setStatus(OtpVerification.OtpStatus.VERIFIED);
        record.setVerifiedAt(LocalDateTime.now());
        otpRepo.save(record);
        // When OTP is VERIFIED
        orderRepo.updateOtpStatus(orderId, OtpVerification.OtpStatus.VERIFIED); // same here
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentType() == Order.PaymentType.PREPAID) {
            orderRepo.updateDeliveryStatus(orderId, Order.DeliveryStatus.DELIVERED);
        }

        return true;
    }
}
