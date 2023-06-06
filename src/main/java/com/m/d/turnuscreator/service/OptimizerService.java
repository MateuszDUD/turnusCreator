package com.m.d.turnuscreator.service;


import com.m.d.turnuscreator.alg.Dijkstra;
import com.m.d.turnuscreator.alg.DijkstraFuzzy;
import com.m.d.turnuscreator.alg.NodesConnectionsCreator;
import com.m.d.turnuscreator.bean.*;
import com.m.d.turnuscreator.enums.FuzzyModelMode;
import com.m.d.turnuscreator.lp.LpTurnusFuzzy;
import com.m.d.turnuscreator.lp.LpTurnusFuzzyHV;
import com.m.d.turnuscreator.lp.LpTurnusFuzzyHVMinEmptyTravels;
import com.m.d.turnuscreator.lp.TurnusCreator;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptimizerService {

    private static double minStep = 0.01;

    private TurnusCreator turnusCreator = new TurnusCreator();

    @Getter
    private int reserve;
    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();
    private Map<Integer, Map<Integer, Integer>> distancesSeconds;
    @Getter
    private Map<Integer, Map<Integer, Integer>> distancesMeters;
    private Map<Integer, Map<Integer, Triple<Long, Long, Long>>> fuzzyDistancesSeconds;
    private List<ScheduleWithPossibleConnections> spojWithPossibleConnectionsList;
    private List<SatisfactionLevel> SatisfactionLevelList;

    private Dijkstra dijkstra = new Dijkstra();
    private DijkstraFuzzy dijkstraFuzzy = new DijkstraFuzzy();

    private int optimisticObjValue;
    private int pessimisticObjValue;
    private double modelSatisfactionLevel;

    private static OptimizerService singleInstance = new OptimizerService();

    public static OptimizerService getInstance() {
        return singleInstance;
    }

    public void init(int m) {
        this.reserve = m;
        this.distancesSeconds = dijkstra.solve(dataRepository.getStopList(), dataRepository.getRouteList(), (route) -> route.getSecondsTriangular().getLeft());
        this.distancesMeters = dijkstra.solve(dataRepository.getStopList(), dataRepository.getRouteList(), Route::getMeters);

        fuzzyDistancesSeconds = dijkstraFuzzy.solve(dataRepository.getStopList(), dataRepository.getRouteList());

        spojWithPossibleConnectionsList =
                NodesConnectionsCreator.createPossibleConnections(dataRepository.getScheduleList(), distancesSeconds);
    }

    public void solveDistMeters() {

        if (this.distancesMeters == null || this.distancesMeters.isEmpty()) {
            this.distancesMeters = dijkstra.solve(dataRepository.getStopList(), dataRepository.getRouteList(), Route::getMeters);
        }
    }

    public ArrayList<SatisfactionLevel> processWithSteps(double step) {
        log.info("processWithSteps() start {}", LocalDateTime.now());
        processOptimisticModel();
        processPessimisticModel();

        double maxSatisfactionLevel = 1.0;
        double currentSLevel = 0.0;

        double currentStep = step;

        List<SatisfactionLevel> results = new ArrayList<>();

        while (currentSLevel <= maxSatisfactionLevel) {

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
                    SatisfactionLevel result = findOptimalWithBiSection(maxSLevel, minSLevel, startSLevel);

                    if (result.getObjectValue() == -1) {
                        break;
                    }

                    if (results.isEmpty()) {
                        results.add(result);
                    } else {
                        SatisfactionLevel lastResult = results.get(results.size() - 1);

                        if (lastResult.getSatisfactionLevelValue() != result.getSatisfactionLevelValue()) {
                            results.add(result);
                        }
                    }
                }

                break;
            } else {
                results.add(SatisfactionLevel.builder().satisfactionLevelValue(currentSLevel).objectValue(lp.getObj_val()).build());
                currentSLevel += currentStep;
                currentSLevel = Math.round(currentSLevel * 100) / 100.0;
            }
        }

        results.forEach(SatisfactionLevel -> {
            log.info("{}", SatisfactionLevel);
        });

        this.SatisfactionLevelList = results;
        log.info("processWithSteps() end {}", LocalDateTime.now());
        return (ArrayList<SatisfactionLevel>) results;
    }

    public SatisfactionLevel findOptimalWithBiSection(double maxSatisfactionLevel, double minSatisfactionLevel, double startSlevel) {
        log.info("findOptimalWithBiSection() start {}", LocalDateTime.now());
        double currentSLevel = startSlevel;

        SatisfactionLevel optimalResult = SatisfactionLevel.builder().objectValue(-1).satisfactionLevelValue(-1).build();

        while (true) {

            LpTurnusFuzzyHV lp = new LpTurnusFuzzyHV();
            lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, pessimisticObjValue, optimisticObjValue, reserve);
            lp.solveModel(currentSLevel);
            log.info("currentSLevel: {}, objVal {}", currentSLevel, lp.getObj_val());


            if (lp.isFeasible()) {
                optimalResult.setObjectValue(lp.getObj_val());
                optimalResult.setSatisfactionLevelValue(currentSLevel);

                minSatisfactionLevel = currentSLevel;
                currentSLevel = Math.round(((maxSatisfactionLevel - minSatisfactionLevel) / 2.0) * 100) / 100.0 + minSatisfactionLevel;
                currentSLevel = Math.round(currentSLevel * 100) / 100.0;

            } else {
                maxSatisfactionLevel = currentSLevel;
                currentSLevel = Math.round(((maxSatisfactionLevel - minSatisfactionLevel) / 2.0) * 100) / 100.0 + minSatisfactionLevel;
                currentSLevel = Math.round(currentSLevel * 100) / 100.0;
            }

            if (currentSLevel == minSatisfactionLevel || currentSLevel == maxSatisfactionLevel) {
                break;
            }
        }

        log.info("findOptimalWithBiSection() end {}", LocalDateTime.now());
        return optimalResult;
    }

    public List<SchedulePlan> processWithoutSteps(Stop depo) {
        log.info("processWithoutSteps() start {}", LocalDateTime.now());
        processOptimisticModel();
        processPessimisticModel();

        double maxSatisfactionLevel = 1.0;
        double minSatisfactionLevel = 0.0;

        SatisfactionLevel result = findOptimalWithBiSection(maxSatisfactionLevel, minSatisfactionLevel, 0.5);
        log.info("{}", result);
        modelSatisfactionLevel = result.getSatisfactionLevelValue();

        LpTurnusFuzzyHVMinEmptyTravels lp = new LpTurnusFuzzyHVMinEmptyTravels();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, distancesMeters, result.getObjectValue(), reserve, depo.getId());
        lp.solveModel(result.getSatisfactionLevelValue());

        List<SchedulePlan> schedulePlanList = turnusCreator.createTurnusListFromGurobiModel(lp.getModel(), spojWithPossibleConnectionsList);

        log.info("processWithoutSteps() end {}", LocalDateTime.now());
        return schedulePlanList;
    }

    private void processOptimisticModel() {
        log.info("processOptimisticModel() start {}", LocalDateTime.now());
        LpTurnusFuzzy lp = new LpTurnusFuzzy();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, FuzzyModelMode.OPTIMISTIC, reserve);
        lp.solveModel();

        optimisticObjValue = (int) lp.getObj_val();
        log.info("optimisticObjValue: {}", optimisticObjValue);
        log.info("processOptimisticModel() end {}", LocalDateTime.now());
    }

    private void processPessimisticModel() {
        log.info("processPessimisticModel() start {}", LocalDateTime.now());
        LpTurnusFuzzy lp = new LpTurnusFuzzy();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, FuzzyModelMode.PESSIMISTIC, reserve);
        lp.solveModel();

        pessimisticObjValue = (int) lp.getObj_val();
        log.info("pessimisticObjValue: {}", pessimisticObjValue);
        log.info("processPessimisticModel() end {}", LocalDateTime.now());
    }

    public List<SchedulePlan> processAtSignLevel(Stop depo, Double m) {

        Optional<SatisfactionLevel> objVal = SatisfactionLevelList.stream().filter(SatisfactionLevel -> SatisfactionLevel.getSatisfactionLevelValue() == m).findFirst();

        if (!objVal.isPresent()) {
            LpTurnusFuzzyHV lp = new LpTurnusFuzzyHV();
            lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, pessimisticObjValue, optimisticObjValue, reserve);
            lp.solveModel(m);

            objVal = Optional.of(SatisfactionLevel.builder().satisfactionLevelValue(m).objectValue(lp.getObj_val()).build());
        }

        log.info("processAtSignLevel() spoj size: {}", spojWithPossibleConnectionsList.size());
        LpTurnusFuzzyHVMinEmptyTravels lp = new LpTurnusFuzzyHVMinEmptyTravels();
        lp.createModel(spojWithPossibleConnectionsList, fuzzyDistancesSeconds, distancesMeters, objVal.get().getObjectValue(), reserve, depo.getId());
        lp.solveModel(objVal.get().getSatisfactionLevelValue());

        List<SchedulePlan> schedulePlanList = turnusCreator.createTurnusListFromGurobiModel(lp.getModel(), spojWithPossibleConnectionsList);

        return schedulePlanList;
    }
}
