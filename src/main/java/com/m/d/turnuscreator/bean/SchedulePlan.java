package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SchedulePlan {

    private int id;
    private List<Schedule> scheduleList;

    private int emptyMeters;
    private int traveledMeters;

    private Stop depot;

    @Override
    public String toString() {
        return "Turnus " + id;
    }

    public void sortSpojList() {
        scheduleList.sort((o1, o2) -> o1.getDeparture().compareTo(o2.getDeparture()));
    }
}
