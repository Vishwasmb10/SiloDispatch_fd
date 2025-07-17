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
@Table("batches")
public class Batch {
    @Id
    private Long id;
    private LocalDateTime createdAt;
    private String clusterLabel;
    private Long driverId;
    private BigDecimal totalWeight;
    private BatchStatus status;

    public enum BatchStatus {
        PENDING, ASSIGNED, IN_PROGRESS, COMPLETED
    }
}
