package com.example.SiloDispatch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("otp_verification")
public class OtpVerification {
    @Id
    private Long id;
    private Long orderId;
    private String otp;
    private LocalDateTime sentAt;
    private LocalDateTime verifiedAt;
    private OtpStatus status;

    public enum OtpStatus { SENT, VERIFIED, EXPIRED }
}
