package com.m.d.turnuscreator.lp;

import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.SpojWithPossibleConnections;
import com.m.d.turnuscreator.bean.Turnus;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class TurnusCreator {

    public List<Turnus> createTurnusListFromGurobiModel(GRBModel model, List<SpojWithPossibleConnections> spojWithPossibleConnectionsList) {

        AtomicInteger turnusId = new AtomicInteger(1);
        List<Turnus> turnusList = new ArrayList<>();

        GRBVar[] gurobiVars = model.getVars();

        int j = 0;

        for (int i = 0; i < gurobiVars.length; i++) {
            try {

                if (gurobiVars[i].get(GRB.DoubleAttr.X) == 1) {
                    String var = gurobiVars[i].get(GRB.StringAttr.VarName);
                    if (var.charAt(0) == 'x' && var.contains("_")) {

                        j++;

                        String[] ids = var.substring(1).split("_");
                        List<Spoj> newSpoj = spojWithPossibleConnectionsList.stream().filter(sp -> {
                            String sId = String.valueOf(sp.getId());
                            return sId.equals(ids[0]) || sId.equals(ids[1]);
                        }).collect(Collectors.toList());

                        List<Turnus> foundTurnusList = turnusList.stream().filter(turnus -> !Collections.disjoint(turnus.getSpojList(), newSpoj)).collect(Collectors.toList());

                        if (foundTurnusList.size() > 2) {
                            log.error("foundTurnusList.size() > 2, size = {}", foundTurnusList.size());
                        } else if (foundTurnusList.size() == 2) {
                            foundTurnusList.get(0).getSpojList().addAll(foundTurnusList.get(1).getSpojList());

                            turnusList.remove(foundTurnusList.get(1));
                        } else if (foundTurnusList.size() == 1) {
                            if (foundTurnusList.get(0).getSpojList().contains(newSpoj.get(0))) {
                                foundTurnusList.get(0).getSpojList().add(newSpoj.get(1));
                            } else {
                                foundTurnusList.get(0).getSpojList().add(newSpoj.get(0));
                            }

                        } else {
                            turnusList.add(Turnus.builder().id(0).spojList(newSpoj).build());
                        }


                    }
                }

            } catch (GRBException e) {
                e.printStackTrace();
            }
        }

        turnusList.forEach(turnus -> {
            turnus.setId(turnusId.getAndIncrement());
            turnus.getSpojList().sort((o1, o2) -> {
                return o1.getDeparture().compareTo(o2.getDeparture());
            });
        });

        return turnusList;
    }
}
