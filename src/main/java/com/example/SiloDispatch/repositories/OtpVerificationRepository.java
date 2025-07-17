package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.OtpVerification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends CrudRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByOrderId(Long orderId);
}

