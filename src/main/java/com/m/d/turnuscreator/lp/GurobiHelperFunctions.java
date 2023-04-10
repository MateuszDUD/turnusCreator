package com.m.d.turnuscreator.lp;

import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GurobiHelperFunctions {

    public static GRBVar[] mapToArray(Map<Integer, Map<Integer, GRBVar>> varMap) {
        List<GRBVar> list = new ArrayList<>();
        varMap.values().forEach(m -> m.values().forEach(i -> list.add(i)));

        return list.toArray(new GRBVar[0]);
    }
}
