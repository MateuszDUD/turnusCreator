package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.SatisfactionLevel;
import com.m.d.turnuscreator.bean.SchedulePlan;
import com.m.d.turnuscreator.bean.Stop;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import com.m.d.turnuscreator.service.OptimizerService;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class SolverViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();

    private OptimizerService optimizerService = OptimizerService.getInstance();

    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {

    }

    public void processWithStep(int m, double step,  Runnable callback) {


        Task<ArrayList<SatisfactionLevel>> task = new Task<ArrayList<SatisfactionLevel>>() {
            @Override
            protected ArrayList<SatisfactionLevel> call() throws Exception {
                optimizerService.init(m);
                return optimizerService.processWithSteps(step);
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            log.info("processWithStep() done size: {}", task.getValue().size());
            dataRepository.setNewSignificanceLevel(task.getValue());
            callback.run();
        });

        task.setOnFailed(workerStateEvent -> {
            log.info("processWithStep() failed");
            callback.run();
        });

        new Thread(task).start();
    }

    public void processWithoutSteps(Stop depo, int m, Runnable callback) {

        Task<ArrayList<SchedulePlan>> task = new Task<ArrayList<SchedulePlan>>() {
            @Override
            protected ArrayList<SchedulePlan> call() throws Exception {
                optimizerService.init(m);
                return (ArrayList<SchedulePlan>) optimizerService.processWithoutSteps(depo);
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            log.info("processWithoutSteps() done size: {}", task.getValue().size());
            task.getValue().forEach(s -> s.setDepot(depo));
            dataRepository.setNewTurnusList(task.getValue());
            callback.run();
        });

        task.setOnFailed(workerStateEvent -> {
            log.info("processWithoutSteps() failed");
            callback.run();
        });

        new Thread(task).start();
    }

    public ObservableList<SatisfactionLevel> getSignificanceLevelList() {
        return dataRepository.getSatisfactionLevelList();
    }

    public void processAtSignLevel(Stop depo, Double m, Runnable callback) {
        Task<ArrayList<SchedulePlan>> task = new Task<ArrayList<SchedulePlan>>() {
            @Override
            protected ArrayList<SchedulePlan> call() throws Exception {
                return (ArrayList<SchedulePlan>) optimizerService.processAtSignLevel(depo, m);
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            log.info("processWithoutSteps() done size: {}", task.getValue().size());
            task.getValue().forEach(s -> s.setDepot(depo));
            dataRepository.setNewTurnusList(task.getValue());
            callback.run();
        });

        task.setOnFailed(workerStateEvent -> {
            log.info("processWithoutSteps() failed");
            callback.run();
        });

        new Thread(task).start();
    }

    public int getScheduleCount() {
        return dataRepository.getScheduleList().size();
    }
}
