package com.example.SiloDispatch.Dto;

import com.example.SiloDispatch.models.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String pincode;
    private String address;
    private BigDecimal distanceKm;
    private BigDecimal weightKg;
    private Order.DeliveryStatus deliveryStatus;
    private Order.PaymentType paymentType;
    private Order.PaymentStatus paymentStatus;
}
