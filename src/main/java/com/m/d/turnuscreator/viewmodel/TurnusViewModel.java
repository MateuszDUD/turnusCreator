package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.Schedule;
import com.m.d.turnuscreator.bean.SchedulePlan;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import com.m.d.turnuscreator.service.OptimizerService;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TurnusViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();

    @Getter
    private ObservableList<SchedulePlan> schedulePlanObservableList = FXCollections.observableArrayList();

    @Getter
    private SchedulePlan selectedSchedulePlan;

    @Getter
    private ObservableList<Schedule> selectedTurnusItemsList = FXCollections.observableArrayList();

    @Getter
    private ObservableList<Schedule> unassignedScheduleObservableList = FXCollections.observableArrayList();

    @Getter
    private final StringProperty textEmptyKm = new SimpleStringProperty("");
    @Getter
    private final StringProperty textTurnusCount = new SimpleStringProperty("");
    @Getter
    private final StringProperty textEmptyKmForTurnus = new SimpleStringProperty("");
    @Getter
    private final StringProperty textUnTurnusCount = new SimpleStringProperty("");
    @Getter
    private final StringProperty textTraveledKmForTurnus = new SimpleStringProperty("");

    private OptimizerService service = OptimizerService.getInstance();

    private List<Schedule> unassignedScheduleList = new ArrayList<>();

    private Boolean filterData = false;

//    ObservableList<Spoj> spojObservableList = FXCollections.observableArrayList();


    private void init() {
        dataRepository.getSchedulePlanObservableList().addListener((ListChangeListener<? super SchedulePlan>) change -> {
            schedulePlanObservableList.clear();
            schedulePlanObservableList.addAll(dataRepository.getSchedulePlanObservableList());

            textTurnusCount.setValue(schedulePlanObservableList.size() + "");
            calculateTotalEmptyKm();
            textUnTurnusCount.setValue("" + unassignedScheduleList.size());
        });

        dataRepository.getUnassignedScheduleObservableList().addListener((InvalidationListener) observable -> {
            unassignedScheduleObservableList.clear();
            unassignedScheduleList.addAll(dataRepository.getUnassignedScheduleObservableList());
            unassignedScheduleObservableList.addAll(dataRepository.getUnassignedScheduleObservableList());
            textUnTurnusCount.setValue("" + unassignedScheduleList.size());
        });
    }

    @Override
    public void onViewAdded() {
        init();

    }

    @Override
    public void onViewRemoved() {

    }

    public void setSelectedSchedulePlan(SchedulePlan t1) {
        this.selectedSchedulePlan = t1;
        selectedTurnusItemsList.clear();
        selectedTurnusItemsList.addAll(t1.getScheduleList());
        this.textEmptyKmForTurnus.setValue("" + Math.round(t1.getEmptyMeters() / 10.0) / 100.0);
        this.textTraveledKmForTurnus.setValue("" + Math.round(t1.getTraveledMeters() / 10.0) / 100.0);
        filerPossibleSpojToAddToTurnus();
    }

    public void createNewTurnus() {
        SchedulePlan maxSchedulePlan = schedulePlanObservableList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get();
        schedulePlanObservableList.add(SchedulePlan.builder().id(maxSchedulePlan != null ? maxSchedulePlan.getId() + 1 : 1).scheduleList(new ArrayList<>()).build());
        textTurnusCount.setValue(schedulePlanObservableList.size() + "");
        filerPossibleSpojToAddToTurnus();
    }

    public void removeTurnus(SchedulePlan t) {
        unassignedScheduleList.addAll(t.getScheduleList());
        unassignedScheduleObservableList.addAll(t.getScheduleList());
        schedulePlanObservableList.remove(t);
        selectedSchedulePlan = null;
        selectedTurnusItemsList.clear();
        textTurnusCount.setValue(schedulePlanObservableList.size() + "");
        calculateTotalEmptyKm();
        textUnTurnusCount.setValue("" + unassignedScheduleList.size());
    }

    public void addSpojToTurnus(SchedulePlan t, Schedule schedule) {
        t.getScheduleList().add(schedule);
        t.sortSpojList();
        selectedTurnusItemsList.clear();
        selectedTurnusItemsList.addAll(t.getScheduleList());

        unassignedScheduleObservableList.remove(schedule);
        unassignedScheduleList.remove(schedule);
        calculateTotalEmptyKm();
        textUnTurnusCount.setValue("" + unassignedScheduleList.size());
        filerPossibleSpojToAddToTurnus();
    }

    public void addRemoveSpojFromTurnus(SchedulePlan t, Schedule schedule) {
        t.getScheduleList().remove(schedule);
        selectedTurnusItemsList.clear();
        selectedTurnusItemsList.addAll(t.getScheduleList());

        unassignedScheduleObservableList.add(schedule);
        unassignedScheduleList.add(schedule);
        calculateTotalEmptyKm();
        textUnTurnusCount.setValue("" + unassignedScheduleList.size());
        filerPossibleSpojToAddToTurnus();
    }

    private void filerPossibleSpojToAddToTurnus() {

        Platform.runLater(() -> {

        List<Schedule> scheduleList = new ArrayList<>();

        if (selectedSchedulePlan.getScheduleList().size() == 0 || !filterData) {
            unassignedScheduleObservableList.clear();
            unassignedScheduleObservableList.addAll(unassignedScheduleList);
            return;
        }

        Map<Integer, Map<Integer, Triple<Long, Long, Long>>> distMatrixSec = service.getFuzzyDistancesSeconds();

        for (int i = 0; i < unassignedScheduleList.size(); i++) {
            List<Schedule> selectedScheduleList = selectedSchedulePlan.getScheduleList();

            int indexAfter = -1;

            for (int j = 0; j < selectedScheduleList.size(); j++) {
                if (unassignedScheduleList.get(i).getDeparture().isBefore(selectedScheduleList.get(j).getDeparture())) {
                    indexAfter = j;
                    break;
                }
            }

            int indexBefore = -1;
            if (indexAfter == -1) {
                indexBefore = selectedScheduleList.size() - 1;
            } else {
                indexBefore = indexAfter - 1;
            }

            boolean canBeAdded = true;

            Schedule b = unassignedScheduleList.get(i);

            if (indexBefore != -1) {

                Schedule a = selectedScheduleList.get(indexBefore);

                long dist = distMatrixSec.get(a.getToId()).get(b.getFromId()).getLeft();

                int arrivalSec = a.getArrival().toSecondOfDay();
                int departureSec = b.getDeparture().toSecondOfDay();

                if (!(arrivalSec + dist <= departureSec)) {
                    canBeAdded = false;
                }
            }

            if (canBeAdded && indexAfter != -1) {
                Schedule c = selectedScheduleList.get(indexAfter);

                long dist = distMatrixSec.get(b.getToId()).get(c.getFromId()).getLeft();

                int arrivalSec = b.getArrival().toSecondOfDay();
                int departureSec = c.getDeparture().toSecondOfDay();

                if (!(arrivalSec + dist < departureSec)) {
                    canBeAdded = false;
                }
            }

            if (canBeAdded) {
                scheduleList.add(b);
            }
        }

        unassignedScheduleObservableList.clear();
        unassignedScheduleObservableList.addAll(scheduleList);
        });

    }

    private void calculateTotalEmptyKm() {

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                AtomicInteger totalEmptyMeters = new AtomicInteger();
                if (!schedulePlanObservableList.isEmpty()) {

                    service.solveDistMeters();

                    Map<Integer, Map<Integer, Integer>> dist = service.getDistancesMeters();

                    schedulePlanObservableList.forEach(turnus -> {
                        int emptyMeters = 0;

                        turnus.sortSpojList();
                        List<Schedule> scheduleList = turnus.getScheduleList();

                        for (int i = 0; i < scheduleList.size() - 1; i++) {
                            emptyMeters += dist.get(scheduleList.get(i).getToId()).get(scheduleList.get(i+1).getFromId());
                        }

                        if (!scheduleList.isEmpty()) {
                            emptyMeters += dist.get(470).get(scheduleList.get(0).getFromId());
                            emptyMeters += dist.get(scheduleList.get(scheduleList.size() - 1).getToId()).get(470);
                        }

                        totalEmptyMeters.addAndGet(emptyMeters);
                        turnus.setEmptyMeters(emptyMeters);

                        //traveled km
                        int traveledDistInKm = scheduleList.stream().mapToInt(Schedule::getDistanceInKm).sum();
                        turnus.setTraveledMeters(traveledDistInKm * 1000);
                    });
                }

                return totalEmptyMeters.get();
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            textEmptyKm.setValue("" + Math.round(task.getValue() / 10.0) / 100.0);

            if (selectedSchedulePlan != null) {
                textEmptyKmForTurnus.setValue("" + Math.round(selectedSchedulePlan.getEmptyMeters() / 10.0) / 100.0);
                textTraveledKmForTurnus.setValue("" + Math.round(selectedSchedulePlan.getTraveledMeters() / 10.0) / 100.0);
            } else {
                textEmptyKmForTurnus.setValue("0");
                textTraveledKmForTurnus.setValue("0");
            }
        });

        new Thread(task).start();
    }

    public void filterData(Boolean newVal) {
        filterData = newVal;
        filerPossibleSpojToAddToTurnus();
    }
}
