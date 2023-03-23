package com.m.d.turnuscreator.repository;

import com.m.d.turnuscreator.bean.Edge;
import com.m.d.turnuscreator.bean.Node;
import com.m.d.turnuscreator.bean.Spoj;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class BaseDataRepository {

    private final static String NODES_P = "\\vrcholy.csv";
    private final static String EDGES_P = "\\hrany.csv";
    private final static String SPOJE_P = "\\spoje.csv";
    
    private String path;

    private ArrayList<Edge> edgeList;
    private ArrayList<Node> nodeList;
    private ArrayList<Spoj> spojList;

    private int maxIdEdge = 0;
    private int maxIdNode = 0;
    private int maxIdConnection = 0;

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

        return true;
    }

    public int getNewNodeId() {
        return ++maxIdNode;
    }

    public int getNewConnectionId() {
        return ++maxIdConnection;
    }

    public void saveData(List<Node> nodes) {
        this.nodeList = nodeList;

        DataReader.saveNodes(path + NODES_P, nodeList);
    }
}
