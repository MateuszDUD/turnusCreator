package com.m.d.turnuscreator.alg;

import com.m.d.turnuscreator.bean.Spoj;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class RandomTriangularTimeEnricher {

    private Random random;

    public RandomTriangularTimeEnricher() {
        random = new Random();
    }

    public RandomTriangularTimeEnricher(long seed) {
        random = new Random(seed);
    }

    public void enrich(List<Spoj> spojList) {
        spojList.forEach(spoj -> {

            int arrivalSec = spoj.getArrival().toSecondOfDay();
            int departureSec = spoj.getDeparture().toSecondOfDay();

            long duration = arrivalSec - departureSec;
            long durationH = duration + random.nextInt(3*60);
            long durationR = durationH + random.nextInt(10*60);

            spoj.setTriangularTimeDurationSec(Triple.of(duration, durationH, durationR));
        });
    }

    public  Map<Integer,Map<Integer, Triple<Long, Long, Long>>> createFuzzyDistanceMatrix(Map<Integer, Map<Integer, Integer>> m) {
        Map<Integer,Map<Integer, Triple<Long, Long, Long>>> fd = new HashMap<>();

        m.entrySet().forEach(from -> {
            fd.putIfAbsent(from.getKey(), new HashMap<>());

            from.getValue().entrySet().forEach(to -> {

                long durationL = to.getValue();
                long durationH = durationL == 0 ? 0 : durationL + random.nextInt(60);
                long durationR = durationL == 0 ? 0 : durationH + random.nextInt(5*60);
                fd.get(from.getKey()).put(to.getKey(),Triple.of(durationL, durationH, durationR));
            });
        });

        return fd;
    }
}
