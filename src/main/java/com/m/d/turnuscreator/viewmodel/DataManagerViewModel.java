package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.Route;
import com.m.d.turnuscreator.bean.Stop;
import com.m.d.turnuscreator.bean.Schedule;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.time.LocalTime;

@Slf4j
public class DataManagerViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository baseDataRepository = BaseDataRepository.getInstance();

    @Getter
    private final StringProperty textInfoDataPath = new SimpleStringProperty("");
    @Getter
    private final StringProperty textInfoStationsCount = new SimpleStringProperty("");
    @Getter
    private final StringProperty textInfoEdgesCount = new SimpleStringProperty("");
    @Getter
    private final StringProperty textInfoConnectionsCount = new SimpleStringProperty("");

    @Getter
    ObservableList<Stop> stopObservableList = FXCollections.observableArrayList();
    @Getter
    ObservableList<Schedule> scheduleObservableList = FXCollections.observableArrayList();
    @Getter
    ObservableList<Route> routeObservableList = FXCollections.observableArrayList();

    public DataManagerViewModel() {
    }

    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {
    }

    public boolean loadDataFromPath(String absolutePath) {
        if (baseDataRepository.loadDataFrom(absolutePath)) {
            textInfoDataPath.setValue(absolutePath);
            setObservables();
            setCounters();

            return true;
        } else {
            return false;
        }
    }

    private void setCounters() {
        textInfoStationsCount.setValue(stopObservableList.size() + "");
        textInfoEdgesCount.setValue("" + routeObservableList.size());
        textInfoConnectionsCount.setValue("" + scheduleObservableList.size());
    }

    public void reloadData() {
        setObservables();
    }

    private void setObservables() {
        stopObservableList.setAll(baseDataRepository.getStopList());
        scheduleObservableList.setAll(baseDataRepository.getScheduleList());
        routeObservableList.setAll(baseDataRepository.getRouteList());
    }

    public void test() {
        baseDataRepository.getStopList().forEach(a -> {
            log.info(a.getName() + " " + a.getId());
        });
    }

    public void removeNode(Stop data) {
        stopObservableList.remove(data);
        setCounters();
    }

    public void removeConnection(Schedule data) {
        scheduleObservableList.remove(data);
        setCounters();
    }

    public void removeEdge(Route data) {
        routeObservableList.remove(data);
        setCounters();
    }

    public void createNewNode() {
        stopObservableList.add(Stop.builder().id(baseDataRepository.getNewNodeId())
                .name("")
                .build());
        setCounters();
    }

    public void createNewEdge() {
        routeObservableList.add(Route.builder()
                .fromId(1)
                .fromName("-")
                .toId(2)
                .toName("-")
                .seconds(0)
                .meters(0)
                .secondsTriangular(Triple.of(0, 0, 0))
                .build());
        setCounters();
    }

    public void createNewConnection() {
        scheduleObservableList.add(Schedule.builder()
                .id(baseDataRepository.getNewConnectionId())
                .line("-1")
                .spoj("-1")
                .fromId(-1)
                .fromName("-")
                .departure(LocalTime.of(0, 0))
                .toId(-1)
                .toName("-")
                .arrival(LocalTime.of(0, 0))
                .distanceInKm(-1)
                .build());
        setCounters();
    }

    public void saveData() {
        baseDataRepository.saveData(this.stopObservableList.stream().toList(),
                this.routeObservableList.stream().toList(),
                this.scheduleObservableList.stream().toList());
    }
}
