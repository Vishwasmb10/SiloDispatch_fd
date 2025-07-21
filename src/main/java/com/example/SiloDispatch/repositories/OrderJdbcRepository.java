package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void insert(Order o) {
        jdbcTemplate.update("""
        INSERT INTO orders(
            id, customer_id, batch_id, driver_id, weight_kg, payment_type, payment_status,
            delivery_status, address, pincode, distance_km, amount, otp_status, created_at,
            lat, lon
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """,
                o.getId(),
                o.getCustomerId(),
                o.getBatchId(),
                o.getDriverId(),
                o.getWeightKg(),
                o.getPaymentType() != null ? o.getPaymentType().name() : null,
                o.getPaymentStatus() != null ? o.getPaymentStatus().name() : null,
                o.getDeliveryStatus() != null ? o.getDeliveryStatus().name() : null,
                o.getAddress(),
                o.getPincode(),
                o.getDistanceKm(),
                o.getAmount(),
                o.getOtpStatus() != null ? o.getOtpStatus().name() : null,
                o.getCreatedAt(),
                o.getLat(),
                o.getLon()
        );
    }

    public void updateOrderBatch(Long orderId, Long batchId) {
        String sql = "UPDATE orders SET batch_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, batchId, orderId);
    }
}