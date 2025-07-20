package com.example.SiloDispatch.schedulers;

import com.example.SiloDispatch.models.OtpVerification;
import com.example.SiloDispatch.repositories.OtpVerificationRepository;
import com.example.SiloDispatch.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpExpiryScheduler {

    private final OtpVerificationRepository otpRepo;
    private final OrderRepository orderRepo;
    private static final int EXPIRY_MINUTES = 1;

    @Scheduled(fixedRate = 60000) // every 1 min
    public void expireOldOtps() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);
        List<OtpVerification> expiredOtps = otpRepo.findAllByStatusAndSentAtBefore(
                OtpVerification.OtpStatus.SENT, cutoffTime);

        for (OtpVerification otp : expiredOtps) {
            otp.setStatus(OtpVerification.OtpStatus.EXPIRED);
            otpRepo.save(otp);
            System.out.println(otp.getOrderId());
            orderRepo.updateOtpStatus(otp.getOrderId(), OtpVerification.OtpStatus.EXPIRED);
            log.info("Expired OTP for orderId {}", otp.getOrderId());
        }
    }
}
