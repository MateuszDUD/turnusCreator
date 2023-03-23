package com.m.d.turnuscreator.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spoj {

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

//    protected Triple<Long, Long, Long> triangularTimeDurationSec;

}
