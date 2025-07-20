package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Payment;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    @Query("SELECT * FROM payments WHERE transaction_id = :transactionId")
    Optional<Payment> findByTransactionId(String transactionId);
}
