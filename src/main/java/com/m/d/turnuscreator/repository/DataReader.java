package com.m.d.turnuscreator.repository;

import com.m.d.turnuscreator.bean.Edge;
import com.m.d.turnuscreator.bean.Node;
import com.m.d.turnuscreator.bean.Spoj;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataReader {

//    private final static String NODES_P = "src/main/resources/liptov/vrcholy.csv";
//    private final static String EDGES_P = "src/main/resources/liptov/hrany.csv";
//    private final static String SPOJE_P = "src/main/resources/liptov/spoje.csv";

    private static DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ISO_TIME;

    public static List<Spoj> readSpoje(String path) throws IOException {
        List<Spoj> spoje = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String departure = values[4].length() == 8 ? values[4] : "0" + values[4];
                String arrival = values[7].length() == 8 ? values[7] : "0" + values[7];

                spoje.add(
                        Spoj.builder()
                                .id(id++)
                                .line(values[0])
                                .spoj(values[1])
                                .fromId(Integer.parseInt(values[2]))
                                .fromName(values[3])
                                .departure(LocalTime.parse(departure))
                                .toId(Integer.parseInt(values[5]))
                                .toName(values[6])
                                .arrival(LocalTime.parse(arrival))
                                .distanceInKm(Integer.parseInt(values[8]))
                                .build()
                );
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return spoje;
    }

    public static List<Edge> readEdges(String path) throws IOException {
        List<Edge> edges = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");


                Edge.EdgeBuilder builder = Edge.builder()
                        .fromId(Integer.parseInt(values[0]))
                        .fromName(values[1])
                        .toId(Integer.parseInt(values[2]))
                        .toName(values[3])
                        .seconds(Integer.parseInt(values[4]))
                        .meters((int) (Double.parseDouble(values[5].replace(',', '.')) * 1000));


                if (values.length == 8) {
                    builder.secondsTriangular(Triple.of(Integer.parseInt(values[4]),
                            Integer.parseInt(values[6]),
                            Integer.parseInt(values[7])));
                } else {
                    builder.secondsTriangular(Triple.of(Integer.parseInt(values[4]),
                            Integer.parseInt(values[4]),
                            Integer.parseInt(values[4])));
                }

                edges.add(builder.build());
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return edges;
    }

    public static List<Node> readNodes(String path) throws IOException {
        List<Node> nodes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                nodes.add(
                        Node.builder()
                                .id(Integer.parseInt(values[0]))
                                .name(values[1])
                                .build()
                );
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return nodes;
    }

    public static void saveNodes(String path, List<Node> nodes) {
//        nodes.sort((o1, o2) -> Integer.compare(o1.getId(), o2.getId()));

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            nodes.stream()
                    .map(node -> prepareCSVRow(node.getId() + "", node.getName()))
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveEdges(String path, ArrayList<Edge> edgeList) {
//        edgeList.sort((o1, o2) -> Integer.compare(o1.getFromId(), o2.getFromId()));

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            edgeList.stream()
                    .map(edge -> prepareCSVRow(
                            String.valueOf(edge.getFromId()),
                            edge.getFromName(),
                            String.valueOf(edge.getToId()),
                            edge.getToName(),
                            String.valueOf(edge.getSecondsTriangular().getLeft()),
                            String.valueOf(edge.getMeters() / 1000.0).replace(".", ","),
                            String.valueOf(edge.getSecondsTriangular().getMiddle()),
                            String.valueOf(edge.getSecondsTriangular().getRight())
                    ))
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveConections(String path, ArrayList<Spoj> spojList) {
//        spojList.sort((o1, o2) -> Integer.compare(Integer.parseInt(o1.getLine()), Integer.parseInt(o2.getLine())));

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            spojList.stream()
                    .map(spoj -> prepareCSVRow(
                            spoj.getLine(),
                            spoj.getSpoj(),
                            String.valueOf(spoj.getFromId()),
                            spoj.getFromName(),
                            spoj.getDeparture().format(isoTimeFormatter),
                            String.valueOf(spoj.getToId()),
                            spoj.getToName(),
                            spoj.getArrival().format(isoTimeFormatter),
                            String.valueOf(spoj.getDistanceInKm())
                    ))
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String prepareCSVRow(String... par) {
        return String.join(";", par);
    }
}
