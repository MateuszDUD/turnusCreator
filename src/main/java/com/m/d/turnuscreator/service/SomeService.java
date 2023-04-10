package com.m.d.turnuscreator.service;


import com.m.d.turnuscreator.alg.Dijkstra;
import com.m.d.turnuscreator.alg.NodesConnectionsCreator;
import com.m.d.turnuscreator.alg.RandomTriangularTimeEnricher;
import com.m.d.turnuscreator.bean.Edge;
import com.m.d.turnuscreator.bean.PlaceHolder;
import com.m.d.turnuscreator.bean.SpojWithPossibleConnections;
import com.m.d.turnuscreator.bean.Turnus;
import com.m.d.turnuscreator.enums.FuzzyModelMode;
import com.m.d.turnuscreator.lp.LpTurnusFuzzy;
import com.m.d.turnuscreator.lp.LpTurnusFuzzyHV;
import com.m.d.turnuscreator.lp.LpTurnusFuzzyHVMinEmptyTravels;
import com.m.d.turnuscreator.lp.TurnusCreator;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Slf4j
public class SomeService {

    private static double minStep = 0.01;

    private TurnusCreator turnusCreator = new TurnusCreator();

    private int reserve;
    private BaseDataRepository dataRepository;
    private Map<Integer, Map<Integer, Integer>> distancesSeconds;
    private Map<Integer, Map<Integer, Integer>> distancesMeters;
    private Map<Integer, Map<Integer, Triple<Long, Long, Long>>> fuzzyDistancesSeconds;
    private List<SpojWithPossibleConnections> spojWithPossibleConnectionsList;
    private List<PlaceHolder> significanceLevelList;

    private Dijkstra dijkstra = new Dijkstra();

    private int optimisticObjValue;
    private int pessimisticObjValue;
    private double modelSignificanceLevel;

    public void init(int m, BaseDataRepository dataRepository) {
        this.dataRepository = dataRepository;
        this.reserve = m;
        this.distancesSeconds = dijkstra.solve(dataRepository.getNodeList(), dataRepository.getEdgeList(), Edge::getSeconds);
        this.distancesMeters = dijkstra.solve(dataRepository.getNodeList(), dataRepository.getEdgeList(), Edge::getMeters);

        RandomTriangularTimeEnricher en = new RandomTriangularTimeEnricher(1);
        en.enrich(dataRepository.getSpojList());
        fuzzyDistancesSeconds = en.createFuzzyDistanceMatrix(distancesSeconds);

        spojWithPossibleConnectionsList =
                NodesConnectionsCreator.createPossibleConnections(dataRepository.getSpojList(), distancesSeconds);
    }

    public ArrayList<PlaceHolder> processWithSteps(double step) {
        processOptimisticModel();
        processPessimisticModel();

        double maxSignificanceLevel = 1.0;
        double currentSLevel = 0.0;

        double currentStep = step;

        List<PlaceHolder> results = new ArrayList<>();

        while (currentSLevel <= maxSignificanceLevel) {

            LpTurnusFuzzyHV lp = new LpTurnusFuzzyHV();
            lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, pessimisticObjValue, optimisticObjValue, reserve);
            lp.solveModel(currentSLevel);
            log.info("currentSLevel: {}, objVal {}", currentSLevel, lp.getObj_val());

            if (!lp.isFeasible()) {

                double minSLevel = currentSLevel - currentStep;
                minSLevel = Math.round(minSLevel * 100) / 100.0;

                double maxSLevel = currentSLevel;

                double startSLevel = minSLevel +  currentStep / 2.0;
                startSLevel =  Math.round(startSLevel * 100) / 100.0;

                if (startSLevel != minSLevel) {
                    PlaceHolder result = findOptimalWithBiSection(maxSLevel, minSLevel, startSLevel);

                    if (result.getObjectValue() == -1) {
                        break;
                    }

                    if (results.isEmpty()) {
                        results.add(result);
                    } else {
                        PlaceHolder lastResult = results.get(results.size() - 1);

                        if (lastResult.getSignificanceLevel() != result.getSignificanceLevel()) {
                            results.add(result);
                        }
                    }
                }

                break;
            } else {
                results.add(PlaceHolder.builder().significanceLevel(currentSLevel).objectValue(lp.getObj_val()).build());
                currentSLevel += currentStep;
                currentSLevel = Math.round(currentSLevel * 100) / 100.0;
            }
        }

        results.forEach(placeHolder -> {
            log.info("{}", placeHolder);
        });

        this.significanceLevelList = results;
        return (ArrayList<PlaceHolder>) results;
    }

    public PlaceHolder findOptimalWithBiSection(double maxSignificanceLevel, double minSignificanceLevel, double startSlevel) {
        double currentSLevel = startSlevel;

        PlaceHolder optimalResult = PlaceHolder.builder().objectValue(-1).significanceLevel(-1).build();

        while (true) {

            LpTurnusFuzzyHV lp = new LpTurnusFuzzyHV();
            lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, pessimisticObjValue, optimisticObjValue, reserve);
            lp.solveModel(currentSLevel);
            log.info("currentSLevel: {}, objVal {}", currentSLevel, lp.getObj_val());


            if (lp.isFeasible()) {
                optimalResult.setObjectValue(lp.getObj_val());
                optimalResult.setSignificanceLevel(currentSLevel);

                minSignificanceLevel = currentSLevel;
                currentSLevel = Math.round(((maxSignificanceLevel - minSignificanceLevel) / 2.0) * 100) / 100.0 + minSignificanceLevel;
                currentSLevel = Math.round(currentSLevel * 100) / 100.0;

            } else {
                maxSignificanceLevel = currentSLevel;
                currentSLevel = Math.round(((maxSignificanceLevel - minSignificanceLevel) / 2.0) * 100) / 100.0 + minSignificanceLevel;
                currentSLevel = Math.round(currentSLevel * 100) / 100.0;
            }

            if (currentSLevel == minSignificanceLevel || currentSLevel == maxSignificanceLevel) {
                break;
            }
        }

        return optimalResult;
    }

    public List<Turnus> processWithoutSteps() {
        processOptimisticModel();
        processPessimisticModel();

        double maxSignificanceLevel = 1.0;
        double minSignificanceLevel = 0.0;

        PlaceHolder result = findOptimalWithBiSection(maxSignificanceLevel, minSignificanceLevel, 0.5);
        log.info("{}", result);
        modelSignificanceLevel = result.getSignificanceLevel();

        LpTurnusFuzzyHVMinEmptyTravels lp = new LpTurnusFuzzyHVMinEmptyTravels();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, distancesMeters, result.getObjectValue(), reserve);
        lp.solveModel(result.getSignificanceLevel());

        List<Turnus> turnusList = turnusCreator.createTurnusListFromGurobiModel(lp.getModel(), spojWithPossibleConnectionsList);

//        turnusList.forEach(turnus -> {
//            log.info("--------------------------------------------------------------------------");
//            log.info("turnus - {}", turnus.getId());
//            turnus.getSpojList().forEach(spoj -> {
//                log.info("{}", spoj);
//            });
//            log.info("--------------------------------------------------------------------------");
//        });

        return turnusList;
    }

    private void processOptimisticModel() {
        LpTurnusFuzzy lp = new LpTurnusFuzzy();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, FuzzyModelMode.OPTIMISTIC, reserve);
        lp.solveModel();

        optimisticObjValue = (int) lp.getObj_val();
        log.info("optimisticObjValue: {}", optimisticObjValue);
    }

    private void processPessimisticModel() {
        LpTurnusFuzzy lp = new LpTurnusFuzzy();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, FuzzyModelMode.PESSIMISTIC, reserve);
        lp.solveModel();

        pessimisticObjValue = (int) lp.getObj_val();
        log.info("pessimisticObjValue: {}", pessimisticObjValue);
    }

    public List<Turnus> processAtSignLevel(Double m) {

        Optional<PlaceHolder> objVal = significanceLevelList.stream().filter(placeHolder -> placeHolder.getSignificanceLevel() == m).findFirst();

        if (!objVal.isPresent()) {
            LpTurnusFuzzyHV lp = new LpTurnusFuzzyHV();
            lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, pessimisticObjValue, optimisticObjValue, reserve);
            lp.solveModel(m);

            objVal = Optional.of(PlaceHolder.builder().significanceLevel(m).objectValue(lp.getObj_val()).build());
        }

        LpTurnusFuzzyHVMinEmptyTravels lp = new LpTurnusFuzzyHVMinEmptyTravels();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, distancesMeters, objVal.get().getObjectValue(), reserve);
        lp.solveModel(objVal.get().getSignificanceLevel());

        List<Turnus> turnusList = turnusCreator.createTurnusListFromGurobiModel(lp.getModel(), spojWithPossibleConnectionsList);

        return turnusList;
    }
}
