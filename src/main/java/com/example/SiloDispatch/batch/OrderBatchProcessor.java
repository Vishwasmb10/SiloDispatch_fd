package com.example.SiloDispatch.batch;

import com.example.SiloDispatch.Dto.OrderForBatching;
import lombok.Getter;
import org.apache.commons.math3.ml.clustering.*;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderBatchProcessor {

    private static final BigDecimal MAX_BATCH_WEIGHT = new BigDecimal("30.0");

    public List<List<OrderForBatching>> formBatches(List<OrderForBatching> orders) {
        Map<String, List<OrderForBatching>> groupedByPincode = orders.stream()
                .collect(Collectors.groupingBy(OrderForBatching::getPincode));

        List<List<OrderForBatching>> finalBatches = new ArrayList<>();

        for (Map.Entry<String, List<OrderForBatching>> entry : groupedByPincode.entrySet()) {
            List<OrderForBatching> pincodeGroup = entry.getValue();

            if (pincodeGroup.size() < 10) {
                // If very few orders, don't bother clustering, just split by weight
                finalBatches.addAll(splitByWeightFFD(pincodeGroup));
                continue;
            }

            // Convert to DistancePoint
            List<DistancePoint> points = pincodeGroup.stream()
                    .map(o -> new DistancePoint(o.getOrderId(), o.getDistance_km()))
                    .collect(Collectors.toList());

            // More conservative number of clusters (1 cluster per 15 orders)
            int numClusters = Math.max(1, pincodeGroup.size() / 15);
            KMeansPlusPlusClusterer<DistancePoint> clusterer =
                    new KMeansPlusPlusClusterer<>(numClusters, 100, new EuclideanDistance());

            List<CentroidCluster<DistancePoint>> clusters = clusterer.cluster(points);

            for (CentroidCluster<DistancePoint> cluster : clusters) {
                List<DistancePoint> clusteredPoints = cluster.getPoints();
                if (clusteredPoints.isEmpty()) continue;

                List<OrderForBatching> clusteredOrders = clusteredPoints.stream()
                        .map(p -> findOrderById(pincodeGroup, p.getOrderId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                finalBatches.addAll(splitByWeight(clusteredOrders));
            }
        }

        return finalBatches;
    }


    private OrderForBatching findOrderById(List<OrderForBatching> list, Long id) {
        return list.stream().filter(o -> o.getOrderId().equals(id)).findFirst().orElse(null);
    }

    private List<List<OrderForBatching>> splitByWeight(List<OrderForBatching> orders) {
        List<List<OrderForBatching>> batches = new ArrayList<>();
        BigDecimal weightLimit = new BigDecimal("30.00");

        // Step 1: Sort in descending order of weight
        List<OrderForBatching> sortedOrders = new ArrayList<>(orders);
        sortedOrders.sort((o1, o2) -> o2.getWeightKg().compareTo(o1.getWeightKg()));

        for (OrderForBatching order : sortedOrders) {
            boolean placed = false;

            // Step 2: Try to place in existing batch
            for (List<OrderForBatching> batch : batches) {
                BigDecimal currentWeight = batch.stream()
                        .map(OrderForBatching::getWeightKg)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (currentWeight.add(order.getWeightKg()).compareTo(weightLimit) <= 0) {
                    batch.add(order);
                    placed = true;
                    break;
                }
            }

            // Step 3: If not placed, create a new batch
            if (!placed) {
                List<OrderForBatching> newBatch = new ArrayList<>();
                newBatch.add(order);
                batches.add(newBatch);
            }
        }

        return batches;
    }

    private List<List<OrderForBatching>> splitByWeightFFD(List<OrderForBatching> orders) {
        List<List<OrderForBatching>> batches = new ArrayList<>();

        // Sort orders by descending weight
        List<OrderForBatching> sortedOrders = new ArrayList<>(orders);
        sortedOrders.sort((o1, o2) -> o2.getWeightKg().compareTo(o1.getWeightKg()));

        for (OrderForBatching order : sortedOrders) {
            boolean placed = false;
            BigDecimal weight = order.getWeightKg();

            // Try placing in an existing batch
            for (List<OrderForBatching> batch : batches) {
                BigDecimal batchWeight = batch.stream()
                        .map(OrderForBatching::getWeightKg)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (batchWeight.add(weight).compareTo(MAX_BATCH_WEIGHT) <= 0) {
                    batch.add(order);
                    placed = true;
                    break;
                }
            }

            // If not placed, start a new batch
            if (!placed) {
                List<OrderForBatching> newBatch = new ArrayList<>();
                newBatch.add(order);
                batches.add(newBatch);
            }
        }

        return batches;
    }



    /**
     * Wrapper for Apache KMeans clustering — clusters by 1D distance field.
     */
    private static class DistancePoint implements Clusterable {
        @Getter
        private final Long orderId;
        private final double distance;

        public DistancePoint(Long orderId, BigDecimal distance) {
            this.orderId = orderId;
            this.distance = distance.doubleValue();
        }

        @Override
        public double[] getPoint() {
            return new double[]{distance};
        }
    }
}
