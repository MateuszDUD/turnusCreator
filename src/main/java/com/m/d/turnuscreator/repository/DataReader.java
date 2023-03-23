package com.m.d.turnuscreator.repository;

import com.m.d.turnuscreator.bean.Edge;
import com.m.d.turnuscreator.bean.Node;
import com.m.d.turnuscreator.bean.Spoj;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataReader {

//    private final static String NODES_P = "src/main/resources/liptov/vrcholy.csv";
//    private final static String EDGES_P = "src/main/resources/liptov/hrany.csv";
//    private final static String SPOJE_P = "src/main/resources/liptov/spoje.csv";

    public static List<Spoj> readSpoje(String path) throws IOException {
        List<Spoj> spoje = new ArrayList<>();

        int max= Integer.MAX_VALUE;

        try (CSVReader csvReader = new CSVReader(new FileReader(path), ';');) {
            String[] values = null;
            csvReader.readNext();

            int id = 1;

            for (int i = 0;(values = csvReader.readNext()) != null && (i < max); i++) {
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
                edges.add(
                        Edge.builder()
                                .fromId(Integer.parseInt(values[0]))
                                .fromName(values[1])
                                .toId(Integer.parseInt(values[2]))
                                .toName(values[3])
                                .seconds(Integer.parseInt(values[4]))
                                .meters((int) (Double.parseDouble(values[5].replace(',', '.')) * 1000))
                                .build()
                );
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
        nodes.sort((o1, o2) -> Integer.compare(o1.getId(), o2.getId()));

        File file = new File(path);
        try {
            FileWriter outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);

            nodes.forEach(node -> {
                String[] districtRow = {
                        String.valueOf(node.getId()),
                        node.getName()
                };

                writer.writeNext(districtRow);
            });

            writer.close();
        } catch (IOException e) {
            log.error("Writing error", e);
        }
    }
}
