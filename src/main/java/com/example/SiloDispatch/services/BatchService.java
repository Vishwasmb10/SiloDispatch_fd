package com.example.SiloDispatch.services;

import com.example.SiloDispatch.Dto.OrderForBatching;
import com.example.SiloDispatch.batch.OrderBatchProcessor;
import com.example.SiloDispatch.models.Batch;
import com.example.SiloDispatch.repositories.BatchRepository;
import com.example.SiloDispatch.repositories.OrderJdbcRepository;
import com.example.SiloDispatch.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final OrderRepository orderRepository;
    private final OrderJdbcRepository orderJdbcRepository;
    private final BatchRepository batchRepository;
    private final OrderBatchProcessor orderBatchProcessor;

    public void generateBatches() {
        List<OrderForBatching> orders = orderRepository.findOrdersForBatching();
//        System.out.println(orders.getFirst());
        List<List<OrderForBatching>> batches = orderBatchProcessor.formBatches(orders);

        for (List<OrderForBatching> batchOrders : batches) {
                BigDecimal totalWeight = batchOrders.stream()
                        .map(OrderForBatching::getWeightKg)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

            Batch batch = new Batch();
            batch.setCreatedAt(LocalDateTime.now());
            batch.setStatus(Batch.BatchStatus.PENDING);
            batch.setTotalWeight(totalWeight);
            batch.setClusterLabel(batchOrders.get(0).getPincode()); // Optional
            Batch saved = batchRepository.save(batch);

            for (OrderForBatching order : batchOrders) {
                orderJdbcRepository.updateOrderBatch(order.getOrderId(), saved.getId());
            }
        }
    }
}

