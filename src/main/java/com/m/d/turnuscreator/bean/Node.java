package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Node {
    private int id;
    private String name;
}
