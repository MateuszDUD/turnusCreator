package com.m.d.turnuscreator.controller;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.m.d.turnuscreator.bean.Schedule;
import com.m.d.turnuscreator.bean.SchedulePlan;
import com.m.d.turnuscreator.viewmodel.TurnusViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class TurnusController implements FxmlView<TurnusViewModel>, Initializable {

    @FXML
    public JFXComboBox comboBoxTurns;
    

    public TableView tableSelectedTurnus;

    @FXML
    public TableView tableUnselectedTurnus;
    @FXML
    public Label labelEmptyKm;
    @FXML
    public Label labelTurnusCount;
    @FXML
    public Label labelEmptyKmForTurnus;
    @FXML
    public JFXCheckBox checkBoxFilterTurnus;
    @FXML
    public Label labelUnTurnusCount;
    @FXML
    public Label labelTraveledKmForTurnus;

    @InjectViewModel
    private TurnusViewModel viewModel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        labelEmptyKm.textProperty().bindBidirectional(viewModel.getTextEmptyKm());
        labelTurnusCount.textProperty().bindBidirectional(viewModel.getTextTurnusCount());
        labelEmptyKmForTurnus.textProperty().bindBidirectional(viewModel.getTextEmptyKmForTurnus());
        labelUnTurnusCount.textProperty().bindBidirectional(viewModel.getTextUnTurnusCount());
        labelTraveledKmForTurnus.textProperty().bindBidirectional(viewModel.getTextTraveledKmForTurnus());

        comboBoxTurns.setItems(viewModel.getSchedulePlanObservableList());

        comboBoxTurns.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> {
            if (t1 instanceof SchedulePlan) {
                log.info("{}", t1);
                viewModel.setSelectedSchedulePlan((SchedulePlan) t1);
            }
        });

        createSpojTable(tableSelectedTurnus);
        tableSelectedTurnus.setItems(viewModel.getSelectedTurnusItemsList());

        createSpojTable(tableUnselectedTurnus);
        tableUnselectedTurnus.setItems(viewModel.getUnassignedScheduleObservableList());

        checkBoxFilterTurnus.selectedProperty().addListener((observableValue, aBoolean, newVal) -> {
            viewModel.filterData(newVal);
        });
        
    }
    
    private void createSpojTable(TableView tableView) {
        int prefWidth = 10;
        
        TableColumn<Schedule, String> idCol = HelperFunctions.createColumnWithIntComparable("ID",
                null,
                data -> new ReadOnlyStringWrapper(data.getValue().getId() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> idLine = HelperFunctions.createColumnWithIntComparable("Linka",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getLine() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> idSpoj = HelperFunctions.createColumnWithIntComparable("Spoj",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getSpoj() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> idFromCol = HelperFunctions.createColumnWithIntComparable("Zastavka od",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getFromId() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> fromCol = HelperFunctions.createColumn("Zastavka od",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getFromName() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> depCol = HelperFunctions.createColumn("Odchod",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getDeparture() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> idToCol = HelperFunctions.createColumnWithIntComparable("Zastavka do",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getToId() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> toCol = HelperFunctions.createColumn("Zastavka do",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getToName() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> arrCol = HelperFunctions.createColumn("Prichod",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getArrival() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Schedule, String> kmCol = HelperFunctions.createColumnWithIntComparable("km",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getDistanceInKm() + ""),
                null,
                prefWidth, tableView);


        tableView.getColumns().addAll(idCol, idLine, idSpoj, idFromCol, fromCol, depCol,
                idToCol, toCol, arrCol,kmCol);
    }

    public void onAddTurnus(ActionEvent actionEvent) {
        viewModel.createNewTurnus();
    }

    public void onDeleteTurnus(ActionEvent actionEvent) {
        if (comboBoxTurns.getSelectionModel().getSelectedItem() instanceof SchedulePlan) {
            SchedulePlan t = (SchedulePlan) comboBoxTurns.getSelectionModel().getSelectedItem();
            log.info("delete turnus {}", t);

            viewModel.removeTurnus(t);
        }
    }

    public void onAddSpojToTurnus(ActionEvent actionEvent) {
        if (comboBoxTurns.getSelectionModel().getSelectedItem() instanceof SchedulePlan) {
            SchedulePlan t = (SchedulePlan) comboBoxTurns.getSelectionModel().getSelectedItem();

            Object obj = tableUnselectedTurnus.getSelectionModel().getSelectedItem();
            if (obj instanceof Schedule) {
                Schedule schedule = (Schedule) obj;
                log.info("onAddSpojToTurnus() {}", schedule);

                viewModel.addSpojToTurnus(t, schedule);
                tableUnselectedTurnus.getSelectionModel().clearSelection();
            }
        }
    }

    public void onRemoveSpojFromTurnus(ActionEvent actionEvent) {
        if (comboBoxTurns.getSelectionModel().getSelectedItem() instanceof SchedulePlan) {
            SchedulePlan t = (SchedulePlan) comboBoxTurns.getSelectionModel().getSelectedItem();

            Object obj = tableSelectedTurnus.getSelectionModel().getSelectedItem();
            if (obj instanceof Schedule) {
                Schedule schedule = (Schedule) obj;
                log.info("onRemoveSpojFromTurnus() {}", schedule);

                viewModel.addRemoveSpojFromTurnus(t, schedule);
                tableSelectedTurnus.getSelectionModel().clearSelection();
            }
        }
    }
}
