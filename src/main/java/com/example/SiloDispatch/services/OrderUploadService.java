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


    public void processCsvOrders(List<OrderCsvDto> orders) {
        for (OrderCsvDto dto : orders) {
            Customer customer = customerRepository.findByPhone(dto.getCustomerPhone());
//            System.out.println(dto.getAddress());
            if (customer == null) {
                customer = new Customer(
                        dto.getCustomerId(),     // This is the manually set ID
                        dto.getCustomerName(),
                        dto.getCustomerPhone(),
                        dto.getAddress(),
                        dto.getPincode(),
                        null,                    // lat
                        null                     // lon
                );
                customerRepository.insert(customer); // 👈 use insert(), NOT save()
            }

            // In case a new customer was inserted, reuse the customerId from DTO
            Long customerId = (customer != null) ? customer.getId() : dto.getCustomerId();
            Order order=orderRepository.findByOrderId(dto.getOrderId());
//            System.out.println("ORDER:" + order.getId());
            if(order!=null){
                return;
            }
            order = new Order();
            order.setId(dto.getOrderId());
            order.setCustomerId(customerId);
            order.setWeightKg(dto.getWeightKg());
            order.setPaymentType(Order.PaymentType.valueOf(dto.getPaymentType().toUpperCase()));
            order.setDeliveryStatus(Order.DeliveryStatus.PENDING);
            order.setPaymentStatus(order.getPaymentType()==Order.PaymentType.PREPAID?Order.PaymentStatus.SUCCESS:Order.PaymentStatus.PENDING);
            order.setAddress(dto.getAddress());
            order.setPincode(dto.getPincode());
            order.setDistanceKm(dto.getDistanceKm());
            order.setAmount(dto.getAmount());

            orderJdbcRepository.insert(order);
        }
    }
}