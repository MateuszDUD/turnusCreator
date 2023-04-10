package com.m.d.turnuscreator.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Turnus {

    private int id;
    private List<Spoj> spojList;

    @Override
    public String toString() {
        return "Turnus " + id;
    }

    public void sortSpojList() {
        spojList.sort((o1, o2) -> o1.getDeparture().compareTo(o2.getDeparture()));
    }
}
