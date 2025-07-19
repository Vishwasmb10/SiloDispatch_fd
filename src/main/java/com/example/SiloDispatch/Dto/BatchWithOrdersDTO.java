package com.example.SiloDispatch.Dto;

import com.example.SiloDispatch.models.Batch;
import lombok.Data;

import java.util.List;

@Data
public class BatchWithOrdersDTO {
    private Long batchId;
    private Batch.BatchStatus status;
    private List<OrderDTO> orders;
}