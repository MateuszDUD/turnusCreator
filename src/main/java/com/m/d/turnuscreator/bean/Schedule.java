package com.m.d.turnuscreator.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    protected int id;
    // Linka
    protected String line;
    // Spoj
    protected String spoj;

    protected int fromId;
    protected String fromName;
    protected LocalTime departure;

    protected int toId;
    protected String toName;
    protected LocalTime arrival;

    protected int distanceInKm;

    protected Triple<Long, Long, Long> triangularTimeDurationSec;

    public long getLeftDuration() {
        int arrivalSec = this.getArrival().toSecondOfDay();
        int departureSec = this.getDeparture().toSecondOfDay();
        return arrivalSec - departureSec;
    }

    public long getMidDuration() {
        return getLeftDuration() + triangularTimeDurationSec.getMiddle();
    }

    public long getRightDuration() {
        return getLeftDuration() + triangularTimeDurationSec.getRight();
    }
}
