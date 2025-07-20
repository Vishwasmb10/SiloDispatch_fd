package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.OtpVerification;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends CrudRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByOrderId(Long orderId);
    Optional<OtpVerification> findTopByOrderIdAndStatusOrderBySentAtDesc(Long orderId, OtpVerification.OtpStatus status);

    @Query("SELECT * FROM otp_verification " +
            "WHERE status = :status " +
            "AND sent_at < :beforeTime")
    List<OtpVerification> findAllByStatusAndSentAtBefore(OtpVerification.OtpStatus status, LocalDateTime beforeTime);

}

