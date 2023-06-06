package com.m.d.turnuscreator.alg;

import com.m.d.turnuscreator.bean.Schedule;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


// debugin purpose
public class RandomTriangularTimeEnricher {

    private Random random;

    public RandomTriangularTimeEnricher() {
        random = new Random();
    }

    public RandomTriangularTimeEnricher(long seed) {
        random = new Random(seed);
    }

    public void enrich(List<Schedule> scheduleList) {
        scheduleList.forEach(spoj -> {
            spoj.setTriangularTimeDurationSec(Triple.of(0L, (long) random.nextInt(2 * 60), (long) random.nextInt(10 * 60)));
        });
    }

    public  Map<Integer,Map<Integer, Triple<Long, Long, Long>>> createFuzzyDistanceMatrix(Map<Integer, Map<Integer, Integer>> m) {
        Map<Integer,Map<Integer, Triple<Long, Long, Long>>> fd = new HashMap<>();

        m.entrySet().forEach(from -> {
            fd.putIfAbsent(from.getKey(), new HashMap<>());

            from.getValue().entrySet().forEach(to -> {

                long durationL = to.getValue();
                long durationH = durationL == 0 ? 0 : durationL + random.nextInt(60);
                long durationR = durationL == 0 ? 0 : durationH + random.nextInt(10*60);
                fd.get(from.getKey()).put(to.getKey(),Triple.of(durationL, durationH, durationR));
            });
        });

        return fd;
    }
}
