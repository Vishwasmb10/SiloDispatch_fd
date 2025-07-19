package com.example.SiloDispatch.util;

import lombok.Getter;

import java.math.BigDecimal;

public class Load {
    @Getter
    private final Long driverId;

    private BigDecimal totalWeight = BigDecimal.ZERO;
    private BigDecimal totalDistance = BigDecimal.ZERO;

    public Load(Long driverId) {
        this.driverId = driverId;
    }

    public void addWeight(BigDecimal weight) {
        totalWeight = totalWeight.add(weight);
    }

    public void addDistance(BigDecimal distance) {
        totalDistance = totalDistance.add(distance);
    }

    public BigDecimal getTotalLoad() {
        return totalWeight.add(totalDistance);
    }
}