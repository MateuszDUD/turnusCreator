package com.m.d.turnuscreator.lp;

import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.SpojWithPossibleConnections;
import gurobi.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class LpTurnusFuzzyHV {

    private Map<Integer,Map<Integer, Triple<Long, Long, Long>>> distances;
    private List<Spoj> spojeSimple;
    private List<SpojWithPossibleConnections> spoje2;

    private GRBModel model;

    private Map<Integer, Map<Integer, GRBVar>> modelVariables;

    private int c_forName = 1;

    private int pessimisticObjVal;
    private int optimisticObjVal;
    private double h = 0;

    @Getter
    private int obj_val= -1;

    @Getter
    private boolean feasible = false;

    private int reserve;

    public LpTurnusFuzzyHV createModel(List<SpojWithPossibleConnections> possibleConnections, Map<Integer,Map<Integer, Triple<Long, Long, Long>>> dist, int pessimisticObjVal, int optimisticObjVal, int reserve) {
        spoje2 = possibleConnections;
        modelVariables = new HashMap<>();
        distances = dist;
        this.reserve = reserve;

        this.pessimisticObjVal = pessimisticObjVal;
        this.optimisticObjVal = optimisticObjVal;

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
        int deb = 0;

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

        model.addConstr(cons, GRB.GREATER_EQUAL, h* optimisticObjVal + (1-h) * pessimisticObjVal, "c" + c_forName++);
    }

    private void createConjiLength() throws GRBException {
        List<GRBLinExpr> consList = new ArrayList<>();
        List<Integer> startTime = new ArrayList<>();

        for (SpojWithPossibleConnections iSpoj : spoje2) {

            if (!iSpoj.getPossibleConnectionsToThis().isEmpty()) {
                GRBLinExpr cons = new GRBLinExpr();


                for (SpojWithPossibleConnections jSpoj : iSpoj.getPossibleConnectionsToThis()) {
                    int coef = 0;
                    coef += jSpoj.getDeparture().toSecondOfDay();
                    coef += jSpoj.getTriangularTimeDurationSec().getLeft();
                    coef += distances.get(jSpoj.getToId()).get(iSpoj.getFromId()).getLeft();
                    coef += reserve;

                    double lz = coef * (1 - h);

                    coef = 0;
                    coef += jSpoj.getDeparture().toSecondOfDay();
                    coef += jSpoj.getTriangularTimeDurationSec().getRight();
                    coef += distances.get(jSpoj.getToId()).get(iSpoj.getFromId()).getRight();
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
        for (SpojWithPossibleConnections iSpoj : spoje2) {

            if (!iSpoj.getPossibleConnectionsToThis().isEmpty()) {
                GRBLinExpr cons = new GRBLinExpr();
                iSpoj.getPossibleConnectionsToThis().forEach(jSpoj
                        -> cons.addTerm(1, modelVariables.get(jSpoj.getId()).get(iSpoj.getId())));
                consList.add(cons);
            }
        }

        for (GRBLinExpr c: consList) {
            model.addConstr(c, GRB.LESS_EQUAL, 1, "c" + c_forName++);
        }

    }

    private void createConij() throws GRBException {
        List<GRBLinExpr> consList = new ArrayList<>();
        for (SpojWithPossibleConnections iSpoj : spoje2) {

            if (!iSpoj.getPossibleConnectionsFromThis().isEmpty()) {
                GRBLinExpr cons = new GRBLinExpr();
                iSpoj.getPossibleConnectionsFromThis().forEach(jSpoj
                        -> cons.addTerm(1, modelVariables.get(iSpoj.getId()).get(jSpoj.getId())));
                consList.add(cons);
            }
        }

        for (GRBLinExpr c: consList) {
            model.addConstr(c, GRB.LESS_EQUAL, 1, "c" + c_forName++);
        }

    }

    private void createObjFunction() throws GRBException {
        GRBLinExpr linExpr = new GRBLinExpr();

        GRBVar[] list = GurobiHelperFunctions.mapToArray(modelVariables);
        double[] coef = new double[list.length];
        Arrays.fill(coef, 1);
        linExpr.addTerms(coef, GurobiHelperFunctions.mapToArray(modelVariables));

        model.setObjective(linExpr, GRB.MAXIMIZE);
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
        });

    }
}
