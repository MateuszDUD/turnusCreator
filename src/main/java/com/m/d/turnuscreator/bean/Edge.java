package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Edge {
    private int fromId;
    private String fromName;

    private int toId;
    private String toName;

    private int seconds;
    private int meters;
}
