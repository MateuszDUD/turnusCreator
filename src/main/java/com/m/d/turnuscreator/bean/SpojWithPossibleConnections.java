package com.m.d.turnuscreator.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SpojWithPossibleConnections extends Spoj {
    private List<SpojWithPossibleConnections> possibleConnectionsFromThis;
    private List<SpojWithPossibleConnections> possibleConnectionsToThis = new ArrayList<>();

    private SpojWithPossibleConnections connectedTo = null;
    private SpojWithPossibleConnections connectedFrom = null;

    public SpojWithPossibleConnections(Spoj spoj) {
        super();
        super.id = spoj.getId();
        super.spoj = spoj.getSpoj();
        super.line = spoj.getLine();
        super.fromId = spoj.getFromId();
        super.fromName = spoj.getFromName();
        super.departure = spoj.getDeparture();
        super.toId = spoj.getToId();
        super.toName = spoj.getToName();
        super.arrival = spoj.getArrival();
        super.distanceInKm = spoj.getDistanceInKm();
        super.triangularTimeDurationSec = spoj.getTriangularTimeDurationSec();
    }
}
