package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.Turnus;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TurnusViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();

    @Getter
    private ObservableList<Turnus> turnusObservableList = FXCollections.observableArrayList();

    @Getter
    private Turnus selectedTurnus;

    @Getter
    private ObservableList<Spoj> selectedTurnusItemsList = FXCollections.observableArrayList();

    @Getter
    private ObservableList<Spoj> unassignedSpojObservableList = FXCollections.observableArrayList();


    private List<Spoj> unassignedSpojList = new ArrayList<>();

//    ObservableList<Spoj> spojObservableList = FXCollections.observableArrayList();


    private void init() {
        dataRepository.getTurnusObservableList().addListener((ListChangeListener<? super Turnus>) change -> {
            turnusObservableList.clear();
            turnusObservableList.addAll(dataRepository.getTurnusObservableList());
        });

        dataRepository.getUnassignedSpojObservableList().addListener((InvalidationListener) observable -> {
            unassignedSpojObservableList.clear();
            unassignedSpojList.addAll(dataRepository.getUnassignedSpojObservableList());
            unassignedSpojObservableList.addAll(dataRepository.getUnassignedSpojObservableList());
        });
    }

    @Override
    public void onViewAdded() {
        init();

    }

    @Override
    public void onViewRemoved() {

    }

    public void setSelectedTurnus(Turnus t1) {
        this.selectedTurnus = t1;
        selectedTurnusItemsList.clear();
        selectedTurnusItemsList.addAll(t1.getSpojList());
    }

    public void createNewTurnus() {
        Turnus maxTurnus = turnusObservableList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get();
        turnusObservableList.add(Turnus.builder().id(maxTurnus != null ? maxTurnus.getId() + 1 : 1).spojList(new ArrayList<>()).build());
    }

    public void removeTurnus(Turnus t) {
        unassignedSpojObservableList.addAll(t.getSpojList());
        turnusObservableList.remove(t);
        selectedTurnus = null;
        selectedTurnusItemsList.clear();
    }

    public void addSpojToTurnus(Turnus t, Spoj spoj) {
        t.getSpojList().add(spoj);
        t.sortSpojList();
        selectedTurnusItemsList.clear();
        selectedTurnusItemsList.addAll(t.getSpojList());

        unassignedSpojObservableList.remove(spoj);
    }

    public void addRemoveSpojFromTurnus(Turnus t, Spoj spoj) {
        t.getSpojList().remove(spoj);
        selectedTurnusItemsList.clear();
        selectedTurnusItemsList.addAll(t.getSpojList());

        unassignedSpojObservableList.add(spoj);
    }

    private void filerPossibleSpojToAddToTurnus() {
        for (int i = 0; i < unassignedSpojList.size(); i++) {
            List<Spoj> selectedSpojList = selectedTurnus.getSpojList();

            //TODO: nehehe
        }
    }
}
