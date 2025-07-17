package com.example.SiloDispatch.util;

import com.example.SiloDispatch.Dto.OrderCsvDto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    public static List<OrderCsvDto> csvToOrders(MultipartFile file) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = new CSVParser(reader,
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim()
                )
        ) {
            List<OrderCsvDto> orders = new ArrayList<>();

            for (CSVRecord record : parser) {
                OrderCsvDto dto = new OrderCsvDto();

                dto.setOrderId(parseLong(record.get("orderId")));
                dto.setCustomerId(parseLong(record.get("customerId")));
                dto.setCustomerName(record.get("customerName"));
                dto.setCustomerPhone(record.get("customerPhone"));
                dto.setAddress(record.get("address"));
                dto.setPincode(record.get("pincode"));
                dto.setDistanceKm(parseBigDecimal(record.get("distanceKm")));
                dto.setAmount(parseBigDecimal(record.get("amount")));
                dto.setWeightKg(parseBigDecimal(record.get("weightKg")));
                dto.setPaymentType(record.get("paymentType"));

                orders.add(dto);
            }
            return orders;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to parse CSV: " + e.getMessage(), e);
        }
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
