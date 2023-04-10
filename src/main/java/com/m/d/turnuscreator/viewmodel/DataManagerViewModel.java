package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.Edge;
import com.m.d.turnuscreator.bean.Node;
import com.m.d.turnuscreator.bean.Spoj;
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
    ObservableList<Node> nodeObservableList = FXCollections.observableArrayList();
    @Getter
    ObservableList<Spoj> spojObservableList = FXCollections.observableArrayList();
    @Getter
    ObservableList<Edge> edgeObservableList = FXCollections.observableArrayList();

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
            textInfoStationsCount.setValue(baseDataRepository.getNodeList().size() + "");
            textInfoEdgesCount.setValue("" + baseDataRepository.getEdgeList().size());
            textInfoConnectionsCount.setValue("" + baseDataRepository.getSpojList().size());
            setObservables();

            return true;
        } else {
            return false;
        }
    }

    public void reloadData() {
        setObservables();
    }

    private void setObservables() {
        nodeObservableList.setAll(baseDataRepository.getNodeList());
        spojObservableList.setAll(baseDataRepository.getSpojList());
        edgeObservableList.setAll(baseDataRepository.getEdgeList());
    }

    public void test() {
        baseDataRepository.getNodeList().forEach(a -> {
            log.info(a.getName() + " " + a.getId());
        });
    }

    public void removeNode(Node data) {
        nodeObservableList.remove(data);
    }

    public void removeConnection(Spoj data) {
        spojObservableList.remove(data);
    }

    public void removeEdge(Edge data) {
        edgeObservableList.remove(data);
    }

    public void createNewNode() {
        nodeObservableList.add(Node.builder().id(baseDataRepository.getNewNodeId())
                .name("")
                .build());
    }

    public void createNewEdge() {
        edgeObservableList.add(Edge.builder()
                .fromId(1)
                .fromName("-")
                .toId(2)
                .toName("-")
                .seconds(0)
                .meters(0)
                        .secondsTriangular(Triple.of(0, 0 ,0))
                .build());
    }

    public void createNewConnection() {
        spojObservableList.add(Spoj.builder()
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
    }

    public void saveData() {
        baseDataRepository.saveData(this.nodeObservableList.stream().toList(),
                this.edgeObservableList.stream().toList(),
                this.spojObservableList.stream().toList());
    }
}
