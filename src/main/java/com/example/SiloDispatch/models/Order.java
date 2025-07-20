package com.example.SiloDispatch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class Order {
    @Id
    private Long id;
    private Long customerId;
    private Long batchId;
    private Long driverId;
    private BigDecimal weightKg;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private DeliveryStatus deliveryStatus;
    private String address;
    private String pincode;
//    private Double lat;
//    private Double lon;
    private BigDecimal distanceKm;
    private BigDecimal amount;
    private OtpStatus otpStatus;
    private LocalDateTime createdAt;

    public enum PaymentType { COD, UPI, PREPAID, UNSPECIFIED}
    public enum PaymentStatus { PENDING, SUCCESS, FAILED }
    public enum DeliveryStatus { PENDING, ARRIVED, DELIVERED }
    public enum OtpStatus { EXPIRED, SENT, VERIFIED }
}

