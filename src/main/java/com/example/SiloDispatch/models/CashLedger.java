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
@Table("cash_ledger")
public class CashLedger {
    @Id
    private Long id;
    private Long driverId;
    private Long orderId;
    private BigDecimal amount;
    private LedgerType type;
    private LocalDateTime timestamp;

    public enum LedgerType { COLLECT, SETTLE }
}
