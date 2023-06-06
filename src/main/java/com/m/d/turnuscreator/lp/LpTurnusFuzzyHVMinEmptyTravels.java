package com.m.d.turnuscreator.lp;

import com.m.d.turnuscreator.bean.Schedule;
import com.m.d.turnuscreator.bean.ScheduleWithPossibleConnections;
import gurobi.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LpTurnusFuzzyHVMinEmptyTravels {

    private Map<Integer, Map<Integer, Triple<Long, Long, Long>>> distancesInSeconds;
    private Map<Integer, Map<Integer, Integer>> distancesMeters;
    private List<Schedule> spojeSimple;
    private List<ScheduleWithPossibleConnections> spoje2;

    @Getter
    private GRBModel model;

    private Map<Integer, Map<Integer, GRBVar>> modelVariables;
    private Map<Integer, GRBVar> modelVariablesU;
    private Map<Integer, GRBVar> modelVariablesV;

    private int depoId = 470;

    private int c_forName = 1;

    private int objVal = 0;
    private double h = 0;

    @Getter
    private int obj_val = -1;

    @Getter
    private boolean feasible = false;

    private int reserve;

    public LpTurnusFuzzyHVMinEmptyTravels createModel(List<ScheduleWithPossibleConnections> possibleConnections,
                                                      Map<Integer, Map<Integer, Triple<Long, Long, Long>>> dist,
                                                      Map<Integer, Map<Integer, Integer>> distMeters, int objVal,
                                                      int reserve, int depoId) {
        spoje2 = possibleConnections;
        modelVariables = new HashMap<>();
        modelVariablesU = new HashMap<>();
        modelVariablesV = new HashMap<>();
        distancesInSeconds = dist;
        this.reserve = reserve;
        this.distancesMeters = distMeters;
        this.depoId = depoId;

        this.objVal = objVal;

        try {
            model = new GRBModel(new GRBEnv());
        } catch (GRBException e) {
            e.printStackTrace();
        }

        return this;
    }

    @SneakyThrows
    public void solveModel(double h) {
        this.h = h;

        createVars();

        createObjFunction();
        createConstr();
        model.optimize();

        feasible = model.get(GRB.IntAttr.Status) == 2;

        if (feasible) {
            obj_val = (int) model.get(GRB.DoubleAttr.ObjVal);
        }
    }

    private void createConstr() throws GRBException {
        createConObjH();

        createConij();
        createConji();

        createConjiLength();
    }

    private void createConObjH() throws GRBException {
        GRBLinExpr cons = new GRBLinExpr();

        GRBVar[] list = GurobiHelperFunctions.mapToArray(modelVariables);

        for (int i = 0; i < list.length; i++) {
            cons.addTerm(1, list[i]);
        }

        model.addConstr(cons, GRB.EQUAL, objVal, "c" + c_forName++);
    }

    private void createConjiLength() throws GRBException {
        List<GRBLinExpr> consList = new ArrayList<>();
        List<Integer> startTime = new ArrayList<>();

        for (ScheduleWithPossibleConnections iSpoj : spoje2) {

            if (!iSpoj.getPossibleConnectionsToThis().isEmpty()) {
                GRBLinExpr cons = new GRBLinExpr();


                for (ScheduleWithPossibleConnections jSpoj : iSpoj.getPossibleConnectionsToThis()) {
                    int coef = 0;
                    coef += jSpoj.getDeparture().toSecondOfDay();
                    coef += jSpoj.getLeftDuration();
                    coef += distancesInSeconds.get(jSpoj.getToId()).get(iSpoj.getFromId()).getLeft();
                    coef += reserve;

                    double lz = coef * (1 - h);

                    coef = 0;
                    coef += jSpoj.getDeparture().toSecondOfDay();
                    coef += jSpoj.getRightDuration();
                    coef += distancesInSeconds.get(jSpoj.getToId()).get(iSpoj.getFromId()).getRight();
                    coef += reserve;

                    double rz = coef * h;

                    cons.addTerm(lz + rz, modelVariables.get(jSpoj.getId()).get(iSpoj.getId()));
                }
                consList.add(cons);
                startTime.add(iSpoj.getDeparture().toSecondOfDay());
            }
        }


        for (int i = 0; i < consList.size(); i++) {
            model.addConstr(consList.get(i), GRB.LESS_EQUAL, startTime.get(i), "c" + c_forName++);
        }
    }

    private void createConji() throws GRBException {
        List<GRBLinExpr> consList = new ArrayList<>();
        for (ScheduleWithPossibleConnections iSpoj : spoje2) {
            GRBLinExpr cons = new GRBLinExpr();

            iSpoj.getPossibleConnectionsToThis().forEach(jSpoj
                    -> cons.addTerm(1, modelVariables.get(jSpoj.getId()).get(iSpoj.getId())));

            cons.addTerm(1, modelVariablesU.get(iSpoj.getId()));
            consList.add(cons);
        }

        for (GRBLinExpr c : consList) {
            model.addConstr(c, GRB.EQUAL, 1, "c" + c_forName++);
        }

    }

    private void createConij() throws GRBException {
        List<GRBLinExpr> consList = new ArrayList<>();
        for (ScheduleWithPossibleConnections iSpoj : spoje2) {
            GRBLinExpr cons = new GRBLinExpr();

            iSpoj.getPossibleConnectionsFromThis().forEach(jSpoj
                    -> cons.addTerm(1, modelVariables.get(iSpoj.getId()).get(jSpoj.getId())));

            cons.addTerm(1, modelVariablesV.get(iSpoj.getId()));
            consList.add(cons);
        }

        for (GRBLinExpr c : consList) {
            model.addConstr(c, GRB.EQUAL, 1, "c" + c_forName++);
        }

    }

    private void createObjFunction() throws GRBException {
        GRBLinExpr linExpr = new GRBLinExpr();

        GRBVar[] list = GurobiHelperFunctions.mapToArray(modelVariables);
        double[] coef = new double[list.length];

        AtomicInteger i = new AtomicInteger();
        for (var entryA : modelVariables.entrySet()) {
            for (var entryB : entryA.getValue().entrySet()) {
                Schedule a = spoje2.stream().filter(s -> s.getId() == entryA.getKey()).findFirst().get();
                Schedule b = spoje2.stream().filter(s -> s.getId() == entryB.getKey()).findFirst().get();
                coef[i.getAndIncrement()] = distancesMeters.get(a.getToId()).get(b.getFromId());
                linExpr.addTerm(distancesMeters.get(a.getToId()).get(b.getFromId()), entryB.getValue());
            }
        }

        for (var entryU : modelVariablesU.entrySet()) {
            Schedule a = spoje2.stream().filter(s -> s.getId() == entryU.getKey()).findFirst().get();
            linExpr.addTerm(distancesMeters.get(depoId).get(a.getFromId()), entryU.getValue());
        }

        for (var entryV : modelVariablesV.entrySet()) {
            Schedule a = spoje2.stream().filter(s -> s.getId() == entryV.getKey()).findFirst().get();
            linExpr.addTerm(distancesMeters.get(a.getToId()).get(depoId), entryV.getValue());
        }


        model.setObjective(linExpr, GRB.MINIMIZE);
    }

    private void createVars() {
        spoje2.forEach(s -> {
            s.getPossibleConnectionsFromThis().forEach(n -> {
                try {
                    GRBVar grbVar = model.addVar(0, 1, 0, GRB.BINARY, "x" + s.getId() + "_" + n.getId());
                    modelVariables.computeIfAbsent(s.getId(), k -> new HashMap<>()).put(n.getId(), grbVar);
                } catch (GRBException e) {
                    e.printStackTrace();
                }
            });

            try {
                GRBVar grbVar = model.addVar(0, 1, 0, GRB.BINARY, "u" + s.getId());
                modelVariablesU.put(s.getId(), grbVar);

                grbVar = model.addVar(0, 1, 0, GRB.BINARY, "v" + s.getId());
                modelVariablesV.put(s.getId(), grbVar);
            } catch (GRBException e) {
                e.printStackTrace();
            }
        });

    }

}
