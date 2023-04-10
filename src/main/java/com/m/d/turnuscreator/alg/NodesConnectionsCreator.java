package com.m.d.turnuscreator.alg;

import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.SpojWithPossibleConnections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NodesConnectionsCreator {

    public static List<SpojWithPossibleConnections> createPossibleConnections(List<Spoj> spojeSimple, Map<Integer, Map<Integer, Integer>> distances) {
        List<SpojWithPossibleConnections> list = new ArrayList<>();

        spojeSimple.forEach(s -> {
            list.add(new SpojWithPossibleConnections(s));
        });

        for (int i = 0; i < list.size(); i++) {
            List<SpojWithPossibleConnections> possibleConnections = new ArrayList<>();

            for (int j = 0; j < list.size(); j++) {
                // from first Node
                SpojWithPossibleConnections a = list.get(i);
                SpojWithPossibleConnections b = list.get(j);

                int dist = distances.get(list.get(i).getToId()).get(list.get(j).getFromId());

                dist += 600;

                int arrivalSec = list.get(i).getArrival().toSecondOfDay();
                int departureSec = list.get(j).getDeparture().toSecondOfDay();

                if (arrivalSec + dist < departureSec && departureSec - arrivalSec < 28*60*60) {
                    possibleConnections.add(list.get(j));
                    list.get(j).getPossibleConnectionsToThis().add(list.get(i));
                }
            }

            list.get(i).setPossibleConnectionsFromThis(possibleConnections);
            list.get(i).getPossibleConnectionsFromThis().sort(Comparator.comparing(Spoj::getDeparture));
            list.get(i).getPossibleConnectionsToThis().sort((o2, o1) -> o1.getArrival().compareTo(o2.getArrival()));
        }

        return list;
    }


//    public static List<SpojWithPossibleConnections> createPossibleFuzzyConnectionsAccordingRightValue(List<Spoj> spojeSimple, Map<Integer, Map<Integer, Integer>> distances) {
//        List<SpojWithPossibleConnections> list = new ArrayList<>();
//
//        spojeSimple.forEach(s -> {
//            list.add(new SpojWithPossibleConnections(s));
//        });
//
//        for (int i = 0; i < list.size(); i++) {
//            List<SpojWithPossibleConnections> possibleConnections = new ArrayList<>();
//
//            for (int j = 0; j < list.size(); j++) {
//                // from first Node
//                SpojWithPossibleConnections a = list.get(i);
//                SpojWithPossibleConnections b = list.get(j);
//
//                int dist = distances.get(list.get(i).getToId()).get(list.get(j).getFromId());
//
//                dist += GlobalConfig.M;
//
//                // fuzzy time traveling
//                int arrivalSec = list.get(i).getDeparture().toSecondOfDay() + list.get(i).getTriangularTimeDurationSec().getRight().intValue();
//
//                int departureSec = list.get(j).getDeparture().toSecondOfDay();
//
//                if (arrivalSec + dist < departureSec && departureSec - arrivalSec < 28*60*60) {
//                    possibleConnections.add(list.get(j));
//                    list.get(j).getPossibleConnectionsToThis().add(list.get(i));
//                }
//            }
//
//            list.get(i).setPossibleConnectionsFromThis(possibleConnections);
//            list.get(i).getPossibleConnectionsFromThis().sort(Comparator.comparing(Spoj::getDeparture));
//            list.get(i).getPossibleConnectionsToThis().sort((o2, o1) -> o1.getArrival().compareTo(o2.getArrival()));
//        }
//
//        return list;
//    }
}
