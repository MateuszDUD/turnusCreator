package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

@Data
@Builder
public class Edge {
    private int fromId;
    private String fromName;

    private int toId;
    private String toName;

    private int seconds;
    private int meters;

    private Triple<Integer, Integer, Integer> secondsTriangular;
}
