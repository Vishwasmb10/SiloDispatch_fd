package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.Dto.OrderForBatching;
import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.models.OtpVerification;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    List<Order> findByBatchId(Long batchId);
    List<Order> findByDriverId(Long driverId);

    @Query("select * from orders where id=:orderId")
    Order findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT id AS order_id, pincode, distance_km, weight_kg FROM orders WHERE batch_id IS NULL")
    List<OrderForBatching> findOrdersForBatching();

    @Modifying
    @Query("update orders set otp_status=:otpStatus where id=:orderId")
    void updateOtpStatus(@Param("orderId") Long orderId,@Param("otpStatus") OtpVerification.OtpStatus otpStatus);

    @Modifying
    @Query("UPDATE orders SET payment_status = :status WHERE id = :orderId")
    void updatePaymentStatus(Long orderId, Order.PaymentStatus status);

//    @Modifying
//    @Query("INSERT INTO orders(id, customerId, batchId, driverId, weightKg, paymentType, paymentStatus, deliveryStatus, address, pincode, lat, lon, otpStatus, createdAt) VALUES (:#{#o.id}, :#{#o.customerId}, :#{#o.batchId}, :#{#o.driverId}, :#{#o.weightKg}, :#{#o.paymentType}, :#{#o.paymentStatus}, :#{#o.deliveryStatus}, :#{#o.address}, :#{#o.pincode}, :#{#o.lat}, :#{#o.lon}, :#{#o.otpStatus}, :#{#o.createdAt})")
//    public void insert(@Param("o")Order o);

}

