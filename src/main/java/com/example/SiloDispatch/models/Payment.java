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
@Table("payments")
public class Payment {
    @Id
    private Long id;
    private Long orderId;
    private Order.PaymentType method;
    private BigDecimal amount;
    private Order.PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
}
