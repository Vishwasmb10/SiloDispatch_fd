package com.example.SiloDispatch.services;

import com.example.SiloDispatch.models.Batch;
import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.repositories.BatchRepository;
import com.example.SiloDispatch.repositories.OrderRepository;
import com.example.SiloDispatch.util.Load;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DriverAssignmentService {

    private final BatchRepository batchRepository;
    private final OrderRepository orderRepository;

    public void assignBatches(List<Long> availableDriverIds) {
        List<Batch> unassignedBatches = batchRepository.findByStatus(Batch.BatchStatus.PENDING);
        Iterator<Batch> batchIterator = unassignedBatches.iterator();

        for (Long driverId : availableDriverIds) {
            if (!batchIterator.hasNext()) break;

            // Check if driver already has an assigned or in-progress batch
            int activeBatchCount = batchRepository.countActiveBatchesForDriver(driverId);
            if (activeBatchCount > 0) continue; // Skip this driver

            Batch batch = batchIterator.next();
            BigDecimal totalDistance = calculateBatchDistance(batch.getId());

            // Assign the batch
            batch.setDriverId(driverId);
            batch.setStatus(Batch.BatchStatus.ASSIGNED);
            batchRepository.save(batch);

            // Update orders in the batch
            List<Order> orders = orderRepository.findByBatchId(batch.getId());
            for (Order order : orders) {
                order.setDriverId(driverId);
            }
            orderRepository.saveAll(orders);
        }
    }


    private BigDecimal calculateBatchDistance(Long batchId) {
        List<Order> orders = orderRepository.findByBatchId(batchId);
        return orders.stream()
                .map(Order::getDistanceKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
