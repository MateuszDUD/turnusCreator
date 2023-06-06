package com.m.d.turnuscreator.repository;

import com.m.d.turnuscreator.bean.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseDataRepository {

    private final static String NODES_P = "\\vrcholy.csv";
    private final static String EDGES_P = "\\hrany.csv";
    private final static String SPOJE_P = "\\spoje.csv";
    private final static String SCHEDULE_PLAN_P = "\\turnus.csv";

    private String path;

    private ArrayList<Route> routeList;
    private ArrayList<Stop> stopList;
    private ArrayList<Schedule> scheduleList;

    ObservableList<Stop> stopObservableList = FXCollections.observableArrayList();

    ObservableList<SchedulePlan> schedulePlanObservableList = FXCollections.observableArrayList();
    ObservableList<Schedule> unassignedScheduleObservableList = FXCollections.observableArrayList();

    ObservableList<SatisfactionLevel> satisfactionLevelList = FXCollections.observableArrayList();

    private int maxIdEdge = 0;
    private int maxIdNode = 0;
    private int maxIdConnection = 0;

    private static BaseDataRepository singleInstance = new BaseDataRepository();

    public static BaseDataRepository getInstance() {
        return singleInstance;
    }

    public boolean loadDataFrom(String absolutePath) {
        try {
            this.path = absolutePath;
            stopList = (ArrayList<Stop>) DataReader.readNodes(absolutePath + NODES_P);
            routeList = (ArrayList<Route>) DataReader.readEdges(absolutePath + EDGES_P);
            scheduleList = (ArrayList<Schedule>) DataReader.readShedules(absolutePath + SPOJE_P);

            stopObservableList.clear();
            stopObservableList.setAll(stopList);

            maxIdNode = stopList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get().getId();
            maxIdConnection = scheduleList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get().getId();
//            maxIdEdge = edgeList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get().getId();


            Task<ArrayList<SchedulePlan>> task = new Task<ArrayList<SchedulePlan>>() {
                @Override
                protected ArrayList<SchedulePlan> call() throws Exception {
                    return DataReader.readSchedulesPlan(absolutePath + SCHEDULE_PLAN_P, stopList, scheduleList);
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                log.info("loadDataFrom() done size: {}", task.getValue().size());
                setNewTurnusList(task.getValue());
            });

            task.setOnFailed(workerStateEvent -> {
                log.info("loadDataFrom() failed");
            });

            new Thread(task).start();

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public int getNewNodeId() {
        return ++maxIdNode;
    }

    public int getNewConnectionId() {
        return ++maxIdConnection;
    }

    public void saveData(List<Stop> stops, List<Route> routes, List<Schedule> scheduleList) {
        this.stopList.clear();
        this.stopList.addAll(stops);
        this.stopObservableList.clear();
        this.stopObservableList.addAll(stops);

        this.routeList.clear();
        this.routeList.addAll(routes);

        this.scheduleList.clear();
        this.scheduleList.addAll(scheduleList);

        DataReader.saveNodes(path + NODES_P, stopList);
        DataReader.saveEdges(path + EDGES_P, routeList);
        DataReader.saveConections(path + SPOJE_P, this.scheduleList);

        if (this.schedulePlanObservableList != null && !this.schedulePlanObservableList.isEmpty()) {
            DataReader.saveShedulePlans(path + SCHEDULE_PLAN_P, schedulePlanObservableList);
        }
    }

    public void setNewTurnusList(ArrayList<SchedulePlan> value) {
        this.schedulePlanObservableList.clear();
        this.schedulePlanObservableList.addAll(value);

        this.unassignedScheduleObservableList.clear();
    }

    public void setNewSignificanceLevel(ArrayList<SatisfactionLevel> value) {
        this.satisfactionLevelList.clear();
        this.satisfactionLevelList.addAll(value);
    }
}
