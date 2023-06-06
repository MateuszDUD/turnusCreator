package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SatisfactionLevel {
    private double satisfactionLevelValue;
    private int objectValue;
}
