package com.m.d.turnuscreator.alg;

import com.m.d.turnuscreator.bean.Route;
import com.m.d.turnuscreator.bean.Stop;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.Function;

public class DijkstraFuzzy {


    public Map<Integer,Map<Integer, Triple<Long, Long, Long>>> solve(List<Stop> stops, List<Route> routes) {
        Map<Integer,Map<Integer, Triple<Long, Long, Long>>> distances = new HashMap<>();

        stops.forEach(n -> {
            distances.put(n.getId(), calculateDistancesFromNode(n.getId(), stops, routes));
        });


        return distances;
    }

    private Map<Integer, Triple<Long, Long, Long>> calculateDistancesFromNode(int nodeId, List<Stop> stops, List<Route> routes) {
        PriorityQueue<DNode> unProcessedNodes = new PriorityQueue<>((o1, o2) -> compare(o1, o2));
        Map<Integer, Triple<Long, Long, Long>> processedNodes = new HashMap<>();

        stops.forEach(n -> {

            if (n.getId() == nodeId) {
                unProcessedNodes.add(DNode.builder()
                        .id(n.getId())
                        .leftVal(0L)
                        .midVal(0L)
                        .rightVal(0L)
                        .build());
            } else {
                unProcessedNodes.add(DNode.builder()
                        .id(n.getId())
                        .leftVal(Long.MAX_VALUE)
                        .midVal(Long.MAX_VALUE)
                        .rightVal(Long.MAX_VALUE)
                        .build());
            }
        });

        while (!unProcessedNodes.isEmpty()) {
            DNode current = unProcessedNodes.poll();

            processedNodes.put(current.id, Triple.of(current.leftVal, current.getMidVal(), current.getRightVal()));

            routes.stream()
                    .filter(e -> e.getFromId() == current.id)
                    .forEach(e -> {
                        Optional<DNode> dn = unProcessedNodes.stream().filter(ee -> {return ee.id == e.getToId();}).findFirst();
                        if (dn.isPresent()) {
                            double dist = getDistFromRute(e) + getDistDnode(current);

                            if (getDistDnode(dn.get()) > dist) {
                                unProcessedNodes.remove(dn.get());

                                dn.get().leftVal = e.getSecondsTriangular().getLeft() + current.getLeftVal();
                                dn.get().midVal = e.getSecondsTriangular().getMiddle() + current.getMidVal();
                                dn.get().rightVal = e.getSecondsTriangular().getRight() + current.getRightVal();

                                unProcessedNodes.add(dn.get());
                            }
                        }
                    });
        }

        return processedNodes;
    }

    @Data
    @Builder
    private static class DNode {
        private int id;
        private Long leftVal;
        private Long midVal;
        private Long rightVal;
    }

    private int compare(DNode a, DNode b) {
        double aDist = getDistDnode(a);
        double bDist = getDistDnode(b);

        return Double.compare(aDist, bDist);
    }

    private double getDistDnode(DNode a) {
        return (a.leftVal + a.midVal + a.rightVal) / 3.0;
    }

    private double getDistFromRute(Route a) {
        return (a.getSecondsTriangular().getLeft() + a.getSecondsTriangular().getMiddle() + a.getSecondsTriangular().getRight()) / 3.0;
    }
}
