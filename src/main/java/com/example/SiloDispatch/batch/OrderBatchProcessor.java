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
        if (orders.size() < 10) {
            return splitByWeightFFD(orders);
        }

        // Convert to LatLonPoint
        List<LatLonPoint> points = orders.stream()
                .map(o -> new LatLonPoint(o.getOrderId(), o.getLat(), o.getLon()))
                .collect(Collectors.toList());

        int numClusters = findOptimalClusterCount(points);

        KMeansPlusPlusClusterer<LatLonPoint> clusterer =
                new KMeansPlusPlusClusterer<>(numClusters, 100, new EuclideanDistance());

        List<CentroidCluster<LatLonPoint>> clusters = clusterer.cluster(points);

        List<List<OrderForBatching>> finalBatches = new ArrayList<>();
        for (CentroidCluster<LatLonPoint> cluster : clusters) {
            List<LatLonPoint> clusteredPoints = cluster.getPoints();
            if (clusteredPoints.isEmpty()) continue;

            List<OrderForBatching> clusteredOrders = clusteredPoints.stream()
                    .map(p -> findOrderById(orders, p.getOrderId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            finalBatches.addAll(splitByWeight(clusteredOrders));
        }

        return finalBatches;
    }

    private OrderForBatching findOrderById(List<OrderForBatching> list, Long id) {
        return list.stream().filter(o -> o.getOrderId().equals(id)).findFirst().orElse(null);
    }

    private List<List<OrderForBatching>> splitByWeight(List<OrderForBatching> orders) {
        List<List<OrderForBatching>> batches = new ArrayList<>();
        orders.sort((o1, o2) -> o2.getWeightKg().compareTo(o1.getWeightKg()));

        for (OrderForBatching order : orders) {
            boolean placed = false;

            for (List<OrderForBatching> batch : batches) {
                BigDecimal currentWeight = batch.stream()
                        .map(OrderForBatching::getWeightKg)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (currentWeight.add(order.getWeightKg()).compareTo(MAX_BATCH_WEIGHT) <= 0) {
                    batch.add(order);
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                List<OrderForBatching> newBatch = new ArrayList<>();
                newBatch.add(order);
                batches.add(newBatch);
            }
        }

        return batches;
    }

    private List<List<OrderForBatching>> splitByWeightFFD(List<OrderForBatching> orders) {
        return splitByWeight(orders);
    }

    private int findOptimalClusterCount(List<LatLonPoint> points) {
        int maxK = Math.min(10, points.size());
        double[] sseList = new double[maxK];

        for (int k = 1; k <= maxK; k++) {
            KMeansPlusPlusClusterer<LatLonPoint> clusterer =
                    new KMeansPlusPlusClusterer<>(k, 100, new EuclideanDistance());
            List<CentroidCluster<LatLonPoint>> clusters = clusterer.cluster(points);

            double sse = 0.0;
            for (CentroidCluster<LatLonPoint> cluster : clusters) {
                double[] center = cluster.getCenter().getPoint();
                for (LatLonPoint p : cluster.getPoints()) {
                    sse += Math.pow(new EuclideanDistance().compute(p.getPoint(), center), 2);
                }
            }
            sseList[k - 1] = sse;
        }

        // Elbow detection
        int elbowK = 1;
        double maxDelta = 0;
        for (int i = 1; i < maxK - 1; i++) {
            double delta = sseList[i - 1] - sseList[i];
            double nextDelta = sseList[i] - sseList[i + 1];
            double diff = delta - nextDelta;

            if (diff > maxDelta) {
                maxDelta = diff;
                elbowK = i + 1;
            }
        }

        return elbowK;
    }

    private static class LatLonPoint implements Clusterable {
        @Getter
        private final Long orderId;
        private final double[] coords;

        public LatLonPoint(Long orderId, BigDecimal lat, BigDecimal lon) {
            this.orderId = orderId;
            this.coords = new double[]{lat.doubleValue(), lon.doubleValue()};
        }

        @Override
        public double[] getPoint() {
            return coords;
        }
    }
}
