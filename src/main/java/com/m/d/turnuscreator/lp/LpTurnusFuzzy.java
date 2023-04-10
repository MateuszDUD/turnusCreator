package com.m.d.turnuscreator.lp;

import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.SpojWithPossibleConnections;
import com.m.d.turnuscreator.enums.FuzzyModelMode;
import gurobi.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

@Slf4j
public class LpTurnusFuzzy {

    private Map<Integer,Map<Integer, Triple<Long, Long, Long>>> distances;
    private List<Spoj> spojeSimple;
    private List<SpojWithPossibleConnections> spoje2;

    private GRBModel model;

    private Map<Integer, Map<Integer, GRBVar>> modelVariables;

    private int c_forName = 1;

    private FuzzyModelMode fuzzyModelMode;

    @Getter
    private double obj_val= -1;
    private int reserve;

    public LpTurnusFuzzy createModel(List<SpojWithPossibleConnections> possibleConnections, Map<Integer, Map<Integer, Triple<Long, Long, Long>>> dist, FuzzyModelMode fuzzyModelMode, int m) {
        spoje2 = possibleConnections;
        modelVariables = new HashMap<>();
        distances = dist;
        this.fuzzyModelMode = fuzzyModelMode;
        this.reserve = m;

        try {
            model = new GRBModel(new GRBEnv());
        } catch (GRBException e) {
            e.printStackTrace();
        }

        return this;
    }

    @SneakyThrows
    public void solveModel() {
        createVars();
        createObjFunction();
        createConstr();
        model.optimize();

        System.out.println(model.get(GRB.DoubleAttr.ObjVal));
        obj_val = model.get(GRB.DoubleAttr.ObjVal);
    }

    private void createConstr() throws GRBException {
        createConij();
        createConji();

        createConjiLength();
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

                    if (fuzzyModelMode == FuzzyModelMode.PESSIMISTIC) {
                        coef += jSpoj.getTriangularTimeDurationSec().getRight();
                        coef += distances.get(jSpoj.getToId()).get(iSpoj.getFromId()).getRight();
                    } else {
                        coef += jSpoj.getTriangularTimeDurationSec().getLeft();
                        coef += distances.get(jSpoj.getToId()).get(iSpoj.getFromId()).getLeft();
                    }

                    coef += reserve;

                    cons.addTerm(coef, modelVariables.get(jSpoj.getId()).get(iSpoj.getId()));
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
