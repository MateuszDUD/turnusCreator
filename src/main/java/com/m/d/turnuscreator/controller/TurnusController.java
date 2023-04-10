package com.m.d.turnuscreator.controller;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.Turnus;
import com.m.d.turnuscreator.viewmodel.ExpViewModel;
import com.m.d.turnuscreator.viewmodel.TurnusViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

@Slf4j
public class TurnusController implements FxmlView<TurnusViewModel>, Initializable {

    @FXML
    public JFXComboBox comboBoxTurns;
    
    @FXML
    public TableView tableSelectedTurnus;
    
    @FXML
    public TableView tableUnselectedTurnus;

    @InjectViewModel
    private TurnusViewModel viewModel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBoxTurns.setItems(viewModel.getTurnusObservableList());

        comboBoxTurns.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> {
            if (t1 instanceof Turnus) {
                log.info("{}", t1);
                viewModel.setSelectedTurnus((Turnus) t1);
            }
        });

        createSpojTable(tableSelectedTurnus);
        tableSelectedTurnus.setItems(viewModel.getSelectedTurnusItemsList());

        createSpojTable(tableUnselectedTurnus);
        tableUnselectedTurnus.setItems(viewModel.getUnassignedSpojObservableList());
        
    }
    
    private void createSpojTable(TableView tableView) {
        int prefWidth = 10;
        
        TableColumn<Spoj, String> idCol = HelperFunctions.createColumnWithIntComparable("ID",
                null,
                data -> new ReadOnlyStringWrapper(data.getValue().getId() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> idLine = HelperFunctions.createColumnWithIntComparable("Linka",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getLine() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> idSpoj = HelperFunctions.createColumnWithIntComparable("Spoj",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getSpoj() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> idFromCol = HelperFunctions.createColumnWithIntComparable("Zastavka od",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getFromId() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> fromCol = HelperFunctions.createColumn("Zastavka od",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getFromName() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> depCol = HelperFunctions.createColumn("Odchod",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getDeparture() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> idToCol = HelperFunctions.createColumnWithIntComparable("Zastavka do",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getToId() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> toCol = HelperFunctions.createColumn("Zastavka do",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getToName() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> arrCol = HelperFunctions.createColumn("Prichod",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getArrival() + ""),
                null,
                prefWidth, tableView);

        TableColumn<Spoj, String> kmCol = HelperFunctions.createColumnWithIntComparable("km",
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
        if (comboBoxTurns.getSelectionModel().getSelectedItem() instanceof Turnus) {
            Turnus t = (Turnus) comboBoxTurns.getSelectionModel().getSelectedItem();
            log.info("delete turnus {}", t);

            viewModel.removeTurnus(t);
        }
    }

    public void onAddSpojToTurnus(ActionEvent actionEvent) {
        if (comboBoxTurns.getSelectionModel().getSelectedItem() instanceof Turnus) {
            Turnus t = (Turnus) comboBoxTurns.getSelectionModel().getSelectedItem();

            Object obj = tableUnselectedTurnus.getSelectionModel().getSelectedItem();
            if (obj instanceof Spoj) {
                Spoj spoj = (Spoj) obj;
                log.info("onAddSpojToTurnus() {}", spoj);

                viewModel.addSpojToTurnus(t, spoj);
                tableUnselectedTurnus.getSelectionModel().clearSelection();
            }
        }
    }

    public void onRemoveSpojFromTurnus(ActionEvent actionEvent) {
        if (comboBoxTurns.getSelectionModel().getSelectedItem() instanceof Turnus) {
            Turnus t = (Turnus) comboBoxTurns.getSelectionModel().getSelectedItem();

            Object obj = tableSelectedTurnus.getSelectionModel().getSelectedItem();
            if (obj instanceof Spoj) {
                Spoj spoj = (Spoj) obj;
                log.info("onRemoveSpojFromTurnus() {}", spoj);

                viewModel.addRemoveSpojFromTurnus(t, spoj);
                tableSelectedTurnus.getSelectionModel().clearSelection();
            }
        }
    }
}
