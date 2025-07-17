package com.example.SiloDispatch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("drivers")
public class Driver {
    @Id
    private Long id;
    private String name;
    private String phone;
    private BigDecimal cashInHand;
    private LocalDateTime createdAt;
}
