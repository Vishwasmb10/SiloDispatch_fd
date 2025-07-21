package com.example.SiloDispatch.services;

import com.example.SiloDispatch.Dto.OrderCsvDto;
import com.example.SiloDispatch.models.Customer;
import com.example.SiloDispatch.models.Order;
import com.example.SiloDispatch.repositories.CustomerRepository;
import com.example.SiloDispatch.repositories.OrderJdbcRepository;
import com.example.SiloDispatch.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderUploadService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderJdbcRepository orderJdbcRepository;
    private final GeoCodingService geoCodingService; // Inject the geocoding service

    public void processCsvOrders(List<OrderCsvDto> orders) {
        for (OrderCsvDto dto : orders) {
            Optional<Customer> optionalCustomer = customerRepository.findById(dto.getCustomerId());

            Customer customer;
            if (optionalCustomer.isPresent()) {
                customer = optionalCustomer.get();
            } else {
                customer = new Customer(
                        dto.getCustomerId(),
                        dto.getCustomerName(),
                        dto.getCustomerPhone(),
                        dto.getAddress(),
                        dto.getPincode()
                );
                customerRepository.insert(customer);
            }

            Long customerId = (customer != null) ? customer.getId() : dto.getCustomerId();
            Order order = orderRepository.findByOrderId(dto.getOrderId());

            if (order != null) {
                continue; // Skip duplicates
            }

            order = new Order();
            order.setId(dto.getOrderId());
            order.setCustomerId(customerId);
            order.setWeightKg(dto.getWeightKg());
            order.setPaymentType(Order.PaymentType.valueOf(dto.getPaymentType().toUpperCase()));
            order.setDeliveryStatus(Order.DeliveryStatus.PENDING);
            order.setPaymentStatus(
                    order.getPaymentType() == Order.PaymentType.PREPAID ?
                            Order.PaymentStatus.SUCCESS :
                            Order.PaymentStatus.PENDING
            );
            order.setAddress(dto.getAddress());
            order.setPincode(dto.getPincode());
            order.setDistanceKm(dto.getDistanceKm());
            order.setAmount(dto.getAmount());

            // 🔍 Extract lat/lon from address using geocoding API
            try {
                double[] latlon = geoCodingService.getLatLonFromAddress(dto.getAddress());
                order.setLat(latlon[0]);
                order.setLon(latlon[1]);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to get lat/lon for address: " + dto.getAddress());
                order.setLat(null);
                order.setLon(null);
            }

            orderJdbcRepository.insert(order);
        }
    }
}
