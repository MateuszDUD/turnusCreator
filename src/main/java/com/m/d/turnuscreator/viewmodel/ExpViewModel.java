package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.PlaceHolder;
import com.m.d.turnuscreator.bean.Turnus;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import com.m.d.turnuscreator.service.SomeService;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class ExpViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();

    private SomeService someService = new SomeService();

    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {

    }

    public void processWithStep(int m, double step,  Runnable callback) {


        Task<ArrayList<PlaceHolder>> task = new Task<ArrayList<PlaceHolder>>() {
            @Override
            protected ArrayList<PlaceHolder> call() throws Exception {
                someService.init(m, dataRepository);
                return someService.processWithSteps(step);
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

    public void processWithoutSteps(int m, Runnable callback) {

        Task<ArrayList<Turnus>> task = new Task<ArrayList<Turnus>>() {
            @Override
            protected ArrayList<Turnus> call() throws Exception {
                someService.init(m, dataRepository);
                return (ArrayList<Turnus>) someService.processWithoutSteps();
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            log.info("processWithoutSteps() done size: {}", task.getValue().size());
            dataRepository.setNewTurnusList(task.getValue());
            callback.run();
        });

        task.setOnFailed(workerStateEvent -> {
            log.info("processWithoutSteps() failed");
            callback.run();
        });

        new Thread(task).start();
    }

    public ObservableList<PlaceHolder> getSignificanceLevelList() {
        return dataRepository.getSignificanceLevelList();
    }

    public void processAtSignLevel(Double m, Runnable callback) {
        Task<ArrayList<Turnus>> task = new Task<ArrayList<Turnus>>() {
            @Override
            protected ArrayList<Turnus> call() throws Exception {
                return (ArrayList<Turnus>) someService.processAtSignLevel(m);
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            log.info("processWithoutSteps() done size: {}", task.getValue().size());
            dataRepository.setNewTurnusList(task.getValue());
            callback.run();
        });

        task.setOnFailed(workerStateEvent -> {
            log.info("processWithoutSteps() failed");
            callback.run();
        });

        new Thread(task).start();
    }
}
