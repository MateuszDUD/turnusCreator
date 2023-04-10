package com.m.d.turnuscreator.repository;

import com.m.d.turnuscreator.bean.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseDataRepository {

    private final static String NODES_P = "\\vrcholy.csv";
    private final static String EDGES_P = "\\hrany.csv";
    private final static String SPOJE_P = "\\spoje.csv";

    private String path;

    private ArrayList<Edge> edgeList;
    private ArrayList<Node> nodeList;
    private ArrayList<Spoj> spojList;

    ObservableList<Turnus> turnusObservableList = FXCollections.observableArrayList();
    ObservableList<Spoj> unassignedSpojObservableList = FXCollections.observableArrayList();

    ObservableList<PlaceHolder> significanceLevelList = FXCollections.observableArrayList();

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
            nodeList = (ArrayList<Node>) DataReader.readNodes(absolutePath + NODES_P);
            edgeList = (ArrayList<Edge>) DataReader.readEdges(absolutePath + EDGES_P);
            spojList = (ArrayList<Spoj>) DataReader.readSpoje(absolutePath + SPOJE_P);

            maxIdNode = nodeList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get().getId();
            maxIdConnection = spojList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get().getId();
//            maxIdEdge = edgeList.stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).get().getId();

        } catch (IOException e) {
            return false;
        }


        //todo: testing delete

        for (int i = 0; i < 5; i++) {
            turnusObservableList.add(Turnus.builder().id(i).spojList(spojList.subList(i * 5, (i + 1) * 5)).build());
        }

        unassignedSpojObservableList.addAll(spojList.subList(5 * 6, spojList.size()));
        // <-
        return true;
    }

    public int getNewNodeId() {
        return ++maxIdNode;
    }

    public int getNewConnectionId() {
        return ++maxIdConnection;
    }

    public void saveData(List<Node> nodes, List<Edge> edges, List<Spoj> spojList) {
        this.nodeList.clear();
        this.nodeList.addAll(nodes);

        this.edgeList.clear();
        this.edgeList.addAll(edges);

        this.spojList.clear();
        this.spojList.addAll(spojList);

        DataReader.saveNodes(path + NODES_P, nodeList);
        DataReader.saveEdges(path + EDGES_P, edgeList);
        DataReader.saveConections(path + SPOJE_P, this.spojList);
    }

    public void setNewTurnusList(ArrayList<Turnus> value) {
        this.turnusObservableList.clear();
        this.turnusObservableList.addAll(value);

        this.unassignedSpojObservableList.clear();
    }

    public void setNewSignificanceLevel(ArrayList<PlaceHolder> value) {
        this.significanceLevelList.clear();
        this.significanceLevelList.addAll(value);
    }
}
