package com.m.d.turnuscreator.repository;

import com.m.d.turnuscreator.bean.Route;
import com.m.d.turnuscreator.bean.SchedulePlan;
import com.m.d.turnuscreator.bean.Stop;
import com.m.d.turnuscreator.bean.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataReader {

//    private final static String NODES_P = "src/main/resources/liptov/vrcholy.csv";
//    private final static String EDGES_P = "src/main/resources/liptov/hrany.csv";
//    private final static String SPOJE_P = "src/main/resources/liptov/spoje.csv";

    private static DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ISO_TIME;

    public static List<Schedule> readShedules(String path) throws IOException {
        List<Schedule> spoje = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String departure = values[4].length() == 8 ? values[4] : "0" + values[4];
                String arrival = values[7].length() == 8 ? values[7] : "0" + values[7];

                long durationMSec = 0L;
                long durationRSec = 0L;

                if (values.length == 11) {
                    durationMSec = Long.parseLong(values[9]);
                    durationRSec = Long.parseLong(values[10]);
                }

                spoje.add(
                        Schedule.builder()
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
                                .triangularTimeDurationSec(Triple.of(0L, durationMSec, durationRSec))
                                .build()
                );
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return spoje;
    }

    public static List<Route> readEdges(String path) throws IOException {
        List<Route> routes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");


                Route.RouteBuilder builder = Route.builder()
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

                routes.add(builder.build());
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return routes;
    }

    public static List<Stop> readNodes(String path) throws IOException {
        List<Stop> stops = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                stops.add(
                        Stop.builder()
                                .id(Integer.parseInt(values[0]))
                                .name(values[1])
                                .build()
                );
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return stops;
    }

    public static ArrayList<SchedulePlan> readSchedulesPlan(String path, List<Stop> stops, List<Schedule> schedules) throws IOException {
        ArrayList<SchedulePlan> schedulePlans = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                log.info("" + line);
                SchedulePlan.SchedulePlanBuilder builder = SchedulePlan.builder();

                builder.id(Integer.parseInt(values[0]));

                int depoId = Integer.parseInt(values[1]);

                builder.depot(stops.stream().filter(stop -> stop.getId() == depoId).findFirst().get());

                ArrayList<Schedule> newScheduleList = new ArrayList<>();

                for (int i = 2; i < values.length; i++) {
                    int scheduleId = Integer.parseInt(values[i]);

                    Schedule schedule = schedules.stream().filter(s -> s.getId() == scheduleId).findFirst().get();
                    newScheduleList.add(schedule);
                }
                builder.scheduleList(newScheduleList);

                schedulePlans.add(builder.build());
            }
        } catch (IOException e) {
            log.error("Error ", e);
            throw e;
        }

        return schedulePlans;
    }

    public static void saveShedulePlans(String path, List<SchedulePlan> schedulePlanList) {
        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            schedulePlanList.stream().map(s -> {
                String scheduleIds = s.getScheduleList().stream().map(a -> a.getId()+ "").collect(Collectors.joining(";"));
                return prepareCSVRow(s.getId() + "",
                        s.getDepot().getId() + "",
                        scheduleIds);
            }).forEach(pw::println);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveNodes(String path, List<Stop> stops) {
//        nodes.sort((o1, o2) -> Integer.compare(o1.getId(), o2.getId()));

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            stops.stream()
                    .map(node -> prepareCSVRow(node.getId() + "", node.getName()))
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveEdges(String path, ArrayList<Route> routeList) {
//        edgeList.sort((o1, o2) -> Integer.compare(o1.getFromId(), o2.getFromId()));

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            routeList.stream()
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

    public static void saveConections(String path, ArrayList<Schedule> scheduleList) {
//        spojList.sort((o1, o2) -> Integer.compare(Integer.parseInt(o1.getLine()), Integer.parseInt(o2.getLine())));

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            scheduleList.stream()
                    .map(spoj -> prepareCSVRow(
                            spoj.getLine(),
                            spoj.getSpoj(),
                            String.valueOf(spoj.getFromId()),
                            spoj.getFromName(),
                            spoj.getDeparture().format(isoTimeFormatter),
                            String.valueOf(spoj.getToId()),
                            spoj.getToName(),
                            spoj.getArrival().format(isoTimeFormatter),
                            String.valueOf(spoj.getDistanceInKm()),
                            String.valueOf(spoj.getTriangularTimeDurationSec().getMiddle()),
                            String.valueOf(spoj.getTriangularTimeDurationSec().getRight())
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
