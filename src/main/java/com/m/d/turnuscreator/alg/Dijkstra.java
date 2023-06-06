package com.m.d.turnuscreator.alg;

import com.m.d.turnuscreator.bean.Route;
import com.m.d.turnuscreator.bean.Stop;
import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.function.Function;

public class Dijkstra {

    private Function<Route, Integer> fun;

    public Map<Integer,Map<Integer, Integer>> solve(List<Stop> stops, List<Route> routes, Function<Route, Integer> fun) {
        Map<Integer,Map<Integer, Integer>> distances = new HashMap<>();
        this.fun = fun;

        stops.forEach(n -> {
            distances.put(n.getId(), calculateDistancesFromNode(n.getId(), stops, routes));
        });


        return distances;
    }

    private Map<Integer, Integer> calculateDistancesFromNode(int nodeId, List<Stop> stops, List<Route> routes) {
        PriorityQueue<DNode> unProcessedNodes = new PriorityQueue<>((o1, o2) -> Integer.compare(o1.value, o2.value));
        Map<Integer, Integer> processedNodes = new HashMap<>();

        stops.forEach(n -> {

            if (n.getId() == nodeId) {
                unProcessedNodes.add(DNode.builder()
                        .id(n.getId())
                        .value(0)
                        .build());
            } else {
                unProcessedNodes.add(DNode.builder()
                        .id(n.getId())
                        .value(Integer.MAX_VALUE)
                        .build());
            }
        });

        while (!unProcessedNodes.isEmpty()) {
            DNode current = unProcessedNodes.poll();

            processedNodes.put(current.id, current.value);

            routes.stream()
                    .filter(e -> e.getFromId() == current.id)
                    .forEach(e -> {
                        Optional<DNode> dn = unProcessedNodes.stream().filter(ee -> {return ee.id == e.getToId();}).findFirst();
                        if (dn.isPresent()) {
                            int dist = fun.apply(e) + current.getValue();

                            if (dn.get().value > dist) {
                                unProcessedNodes.remove(dn.get());
                                dn.get().value = dist;
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
        private int value;
    }
}
