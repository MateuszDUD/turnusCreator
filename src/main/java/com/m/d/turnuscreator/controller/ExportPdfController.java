package com.m.d.turnuscreator.controller;

import com.m.d.turnuscreator.bean.SchedulePlan;
import com.m.d.turnuscreator.viewmodel.ExportPdfViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ExportPdfController implements FxmlView<ExportPdfViewModel>, Initializable {

    @FXML
    public TableView tableNotToExport;

    @FXML
    public TableView tableToExport;

    private Stage stage;

    @InjectViewModel
    private ExportPdfViewModel viewModel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTable(tableNotToExport, viewModel.getSchedulePlanNotToExportObservableList(), "Turnusy");
        initTable(tableToExport, viewModel.getSchedulePlanToExportObservableList(), "Turnusy pre export");
    }

    private void initTable(TableView tableView, ObservableList<SchedulePlan> observableList, String name) {
        TableColumn<SchedulePlan, String> col = HelperFunctions.createColumn(name,
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().toString()),
                null,
                1, tableView);

        tableView.getColumns().add(col);
        tableView.setItems(observableList);
    }

    public void onAddAll(ActionEvent actionEvent) {
        viewModel.addToExportAll();
    }

    public void onAddOne(ActionEvent actionEvent) {
        Object obj = tableNotToExport.getSelectionModel().getSelectedItem();
        if (obj instanceof SchedulePlan) {
            SchedulePlan schedulePlan = (SchedulePlan) obj;
            viewModel.addOneToExport(schedulePlan);
        }
    }

    public void onExport(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File path = directoryChooser.showDialog(stage);
        if (path != null) {
            viewModel.export(path.getAbsolutePath());
        }
    }

    public void oneRemoveOne(ActionEvent actionEvent) {
        Object obj = tableToExport.getSelectionModel().getSelectedItem();
        if (obj instanceof SchedulePlan) {
            SchedulePlan schedulePlan = (SchedulePlan) obj;
            viewModel.removeOneFromExport(schedulePlan);
        }
    }

    public void onRemoveAll(ActionEvent actionEvent) {
        viewModel.removeFromExportAll();
    }
}
