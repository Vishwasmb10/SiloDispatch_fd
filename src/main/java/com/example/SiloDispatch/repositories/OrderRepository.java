package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    List<Order> findByBatchId(Long batchId);
    List<Order> findByDriverId(Long driverId);
}

