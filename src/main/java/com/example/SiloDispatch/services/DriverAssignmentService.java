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
        Map<Long, Load> driverLoadMap = new HashMap<>();

        for (Long driverId : availableDriverIds) {
            driverLoadMap.put(driverId, new Load(driverId));
        }

        for (Batch batch : unassignedBatches) {
            BigDecimal totalDistance = calculateBatchDistance(batch.getId());

            Long bestDriverId = driverLoadMap.values().stream()
                    .min(Comparator.comparing(Load::getTotalLoad))
                    .map(Load::getDriverId)
                    .orElseThrow();

            // Update batch
            batch.setDriverId(bestDriverId);
            batch.setStatus(Batch.BatchStatus.ASSIGNED);
            batchRepository.save(batch);

            // ✅ Also update all orders in the batch
            List<Order> orders = orderRepository.findByBatchId(batch.getId());
            for (Order order : orders) {
                order.setDriverId(bestDriverId);
            }
            orderRepository.saveAll(orders);  // Save all updated orders

            // Update driver's load
            Load load = driverLoadMap.get(bestDriverId);
            load.addWeight(batch.getTotalWeight());
            load.addDistance(totalDistance);
        }

    }

    private BigDecimal calculateBatchDistance(Long batchId) {
        List<Order> orders = orderRepository.findByBatchId(batchId);
        return orders.stream()
                .map(Order::getDistanceKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
