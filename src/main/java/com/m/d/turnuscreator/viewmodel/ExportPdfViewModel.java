package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.Schedule;
import com.m.d.turnuscreator.bean.SchedulePlan;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;


public class ExportPdfViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();

    @Getter
    private ObservableList<SchedulePlan> schedulePlanToExportObservableList = FXCollections.observableArrayList();

    @Getter
    private ObservableList<SchedulePlan> schedulePlanNotToExportObservableList = FXCollections.observableArrayList();


    private void init() {
        dataRepository.getSchedulePlanObservableList().addListener((ListChangeListener<? super SchedulePlan>) change -> {
            schedulePlanNotToExportObservableList.clear();
            schedulePlanNotToExportObservableList.addAll(dataRepository.getSchedulePlanObservableList());
        });
    }

    @Override
    public void onViewAdded() {
        init();

    }

    @Override
    public void onViewRemoved() {

    }

    public void addToExportAll() {
        schedulePlanToExportObservableList.addAll(schedulePlanNotToExportObservableList);
        schedulePlanNotToExportObservableList.clear();
    }

    public void removeFromExportAll() {
        schedulePlanNotToExportObservableList.addAll(schedulePlanToExportObservableList);
        schedulePlanToExportObservableList.clear();
    }

    public void addOneToExport(SchedulePlan schedulePlan) {
        schedulePlanToExportObservableList.add(schedulePlan);
        schedulePlanNotToExportObservableList.remove(schedulePlan);
    }

    public void removeOneFromExport(SchedulePlan schedulePlan) {
        schedulePlanNotToExportObservableList.add(schedulePlan);
        schedulePlanToExportObservableList.remove(schedulePlan);
    }

    @SneakyThrows
    public void export(String absolutePath) {
        List<SchedulePlan> schedulePlans = schedulePlanToExportObservableList.stream().toList();

        try (PDDocument document = new PDDocument()) {
            PDFont formFont = PDType0Font.load(document, new FileInputStream("arial.ttf"), false);
            for (int i = 0; i < schedulePlans.size(); i++) {
                SchedulePlan sheSchedulePlan = schedulePlans.get(i);


                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                    // Build the table
                    Table.TableBuilder myTable = Table.builder()
                            .addColumnsOfWidth(60, 50, 120, 55, 120, 55, 40, 60)
                            .padding(8)
                            .font(formFont)
                            .fontSize(10)
                            .addRow(Row.builder()
                                    .add(TextCell.builder().text("Turnus " + sheSchedulePlan.getId()).colSpan(8).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build())
                                    .build());

                    myTable.addRow(
                            Row.builder()
                                    .add(
                                            TextCell.builder().text("Linka").borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build()
                                    )
                                    .add(
                                            TextCell.builder().text("Spoj").borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build()
                                    )
                                    .add(
                                            TextCell.builder().text("Začiatok").colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build()
                                    )
                                    .add(
                                            TextCell.builder().text("Koniec").colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build()
                                    )
                                    .add(
                                            TextCell.builder().text("KM").borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build()
                                    )
                                    .add(
                                            TextCell.builder().text("Čas").borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build()
                                    ).build()
                    );

                    List<Schedule> scheduleList = sheSchedulePlan.getScheduleList();

                    for (int j = 0; j < scheduleList.size(); j++) {
                        Schedule schedule = scheduleList.get(j);

                        myTable.addRow(
                                Row.builder()
                                        .add(
                                                TextCell.builder().text(schedule.getLine()).borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder().text(schedule.getSpoj()).borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder().text(schedule.getFromName()).borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder().text(schedule.getDeparture() + "").borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder().text(schedule.getToName()).borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder().text(schedule.getArrival() + "").borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder().text(schedule.getDistanceInKm() + "").borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        )
                                        .add(
                                                TextCell.builder()
                                                        .text(DurationFormatUtils.formatDuration(Duration.between(schedule.getDeparture(), schedule.getArrival()).toMillis(), "HH:mm:ss", true))
                                                        .borderWidth(1).borderColorLeft(Color.BLACK).build()
                                        ).build()
                        );
                    }

                    myTable.addRow(Row.builder()
                            .add(TextCell.builder().text("").colSpan(8).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.DARK_GRAY).build())
                            .build());

                    myTable.addRow(Row.builder()
                            .add(TextCell.builder().text("Depo").colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build())
                            .add(TextCell.builder().text("Prejdene km").colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build())
                            .add(TextCell.builder().text("Prazdne km").colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build())
                            .add(TextCell.builder().text("Celkove km").colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).backgroundColor(Color.lightGray).build())
                            .build());

                    myTable.addRow(Row.builder()
                            .add(TextCell.builder().text(sheSchedulePlan.getDepot().getName()).colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).build())
                            .add(TextCell.builder().text("" + Math.round(sheSchedulePlan.getTraveledMeters() / 10.0) / 100.0).colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).build())
                            .add(TextCell.builder().text("" + Math.round(sheSchedulePlan.getEmptyMeters() / 10.0) / 100.0).colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).build())
                            .add(TextCell.builder().text("" + Math.round((sheSchedulePlan.getEmptyMeters() + sheSchedulePlan.getTraveledMeters()) / 10.0) / 100.0).colSpan(2).borderWidth(1).borderColorLeft(Color.BLACK).build())
                            .build());


                    // Set up the drawer
                    TableDrawer tableDrawer = TableDrawer.builder()
                            .contentStream(contentStream)
                            .startX(20f)
                            .startY(page.getMediaBox().getUpperRightY() - 20f)
                            .table(myTable.build())
                            .build();

                    // And go for it!
                    tableDrawer.draw();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            document.save(absolutePath + "/export_data.pdf");
        }
    }
}
