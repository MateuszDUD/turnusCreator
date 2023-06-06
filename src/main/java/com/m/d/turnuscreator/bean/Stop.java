package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Stop {
    private int id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
