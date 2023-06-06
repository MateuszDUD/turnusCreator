package com.m.d.turnuscreator.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ScheduleWithPossibleConnections extends Schedule {
    private List<ScheduleWithPossibleConnections> possibleConnectionsFromThis;
    private List<ScheduleWithPossibleConnections> possibleConnectionsToThis = new ArrayList<>();

    public ScheduleWithPossibleConnections(Schedule schedule) {
        super();
        super.id = schedule.getId();
        super.spoj = schedule.getSpoj();
        super.line = schedule.getLine();
        super.fromId = schedule.getFromId();
        super.fromName = schedule.getFromName();
        super.departure = schedule.getDeparture();
        super.toId = schedule.getToId();
        super.toName = schedule.getToName();
        super.arrival = schedule.getArrival();
        super.distanceInKm = schedule.getDistanceInKm();
        super.triangularTimeDurationSec = schedule.getTriangularTimeDurationSec();
    }
}
