package com.example.SiloDispatch.services;

import com.example.SiloDispatch.Dto.BatchWithOrdersDTO;
import com.example.SiloDispatch.Dto.DriverDto;
import com.example.SiloDispatch.Dto.OrderDTO;
import com.example.SiloDispatch.models.Batch;
import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.repositories.BatchRepository;
import com.example.SiloDispatch.repositories.CustomerRepository;
import com.example.SiloDispatch.repositories.DriverRepository;
import com.example.SiloDispatch.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverViewService {

    private final OrderRepository orderRepository;
    private final BatchRepository batchRepository;
    private final DriverRepository driverRepository;
    private final CustomerRepository customerRepository;

    public List<BatchWithOrdersDTO> getOrdersGroupedByBatch(Long driverId) {
        List<Order> orders = orderRepository.findByDriverId(driverId);

        // Group orders by batch ID
        Map<Long, List<Order>> batchOrderMap = orders.stream()
                .collect(Collectors.groupingBy(Order::getBatchId));

        List<BatchWithOrdersDTO> result = new ArrayList<>();

        for (Map.Entry<Long, List<Order>> entry : batchOrderMap.entrySet()) {
            Long batchId = entry.getKey();
            List<Order> batchOrders = entry.getValue();

            Batch batch = batchRepository.findById(batchId).orElse(null);
            if (batch == null) continue;

            BatchWithOrdersDTO dto = new BatchWithOrdersDTO();
            dto.setBatchId(batchId);
            dto.setStatus(batch.getStatus());

            List<OrderDTO> orderDTOs = batchOrders.stream()
                    .map(order -> {
                        String phone = customerRepository.findPhoneById(order.getCustomerId());
                        return new OrderDTO(
                                order.getId(),
                                order.getPincode(),
                                order.getAddress(),
                                order.getDistanceKm(),
                                order.getWeightKg(),
                                order.getDeliveryStatus(),
                                order.getPaymentType(),
                                order.getPaymentStatus(),
                                order.getOtpStatus(),
                                phone,
                                order.getAmount()
                        );
                    })
                    .collect(Collectors.toList());

            dto.setOrders(orderDTOs);
            result.add(dto);
        }

        return result;
    }

    public List<DriverDto> getAvailableDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(driver -> new DriverDto(driver.getId(), driver.getName()))
                .collect(Collectors.toList());
    }
}
