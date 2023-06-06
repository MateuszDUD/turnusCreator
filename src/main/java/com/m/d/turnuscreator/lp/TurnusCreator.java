package com.m.d.turnuscreator.lp;

import com.m.d.turnuscreator.bean.Schedule;
import com.m.d.turnuscreator.bean.ScheduleWithPossibleConnections;
import com.m.d.turnuscreator.bean.SchedulePlan;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class TurnusCreator {

    public List<SchedulePlan> createTurnusListFromGurobiModel(GRBModel model, List<ScheduleWithPossibleConnections> spojWithPossibleConnectionsList) {

        AtomicInteger turnusId = new AtomicInteger(1);
        List<SchedulePlan> schedulePlanList = new ArrayList<>();

        GRBVar[] gurobiVars = model.getVars();

        int j = 0;
        int u_count = 0;
        int v_count = 0;

        spojWithPossibleConnectionsList.forEach(spoj -> {
            List<Schedule> s = new ArrayList<>();
            s.add(spoj);
            schedulePlanList.add(SchedulePlan.builder().id(0).scheduleList(s).build());
        });


        for (int i = 0; i < gurobiVars.length; i++) {
            try {

                if (gurobiVars[i].get(GRB.DoubleAttr.X) == 1) {
                    String var = gurobiVars[i].get(GRB.StringAttr.VarName);
                    if (var.charAt(0) == 'x' && var.contains("_")) {

                        j++;

                        String[] ids = var.substring(1).split("_");
                        List<Schedule> newSchedule = spojWithPossibleConnectionsList.stream().filter(sp -> {
                            String sId = String.valueOf(sp.getId());
                            return sId.equals(ids[0]) || sId.equals(ids[1]);
                        }).collect(Collectors.toList());

                        List<SchedulePlan> foundSchedulePlanList = schedulePlanList.stream().filter(turnus -> !Collections.disjoint(turnus.getScheduleList(), newSchedule)).collect(Collectors.toList());

                        if (foundSchedulePlanList.size() > 2) {
                            log.error("foundTurnusList.size() > 2, size = {}", foundSchedulePlanList.size());
                        } else if (foundSchedulePlanList.size() == 2) {
                            foundSchedulePlanList.get(0).getScheduleList().addAll(foundSchedulePlanList.get(1).getScheduleList());

                            schedulePlanList.remove(foundSchedulePlanList.get(1));
                        } else if (foundSchedulePlanList.size() == 1) {
                            if (foundSchedulePlanList.get(0).getScheduleList().contains(newSchedule.get(0))) {
                                foundSchedulePlanList.get(0).getScheduleList().add(newSchedule.get(1));
                            } else {
                                foundSchedulePlanList.get(0).getScheduleList().add(newSchedule.get(0));
                            }

                        } else {
                            schedulePlanList.add(SchedulePlan.builder().id(0).scheduleList(newSchedule).build());
                        }


                    }

                    if (var.charAt(0) == 'u') {
                        log.info("u {}", var);
                        u_count++;
                    } else if (var.charAt(0) == 'v') {
                        log.info("v {}", var);
                        v_count++;
                    }
                }

            } catch (GRBException e) {
                log.error("createTurnusListFromGurobiModel() error", e);
                return null;
            }
        }
        log.info("u {} v {} j {}", u_count, v_count, j);

        log.info("createTurnusListFromGurobiModel(), turnusList.size = {}", schedulePlanList.size());

        AtomicInteger i = new AtomicInteger();
        Set<Integer> st = new HashSet<>();

        schedulePlanList.forEach(a -> a.getScheduleList().forEach(b -> {
            i.getAndIncrement();
            st.add(b.getId());
        }));

        schedulePlanList.forEach(turnus -> {
            turnus.setId(turnusId.getAndIncrement());
            turnus.getScheduleList().sort((o1, o2) -> {
                return o1.getDeparture().compareTo(o2.getDeparture());
            });
        });

        return schedulePlanList;
    }
}
