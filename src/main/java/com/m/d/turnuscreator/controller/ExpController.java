package com.m.d.turnuscreator.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.m.d.turnuscreator.bean.PlaceHolder;
import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.viewmodel.ExpViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class ExpController implements FxmlView<ExpViewModel>, Initializable {
    @FXML
    public JFXTextField aaa;
    @FXML
    public JFXTextField textStep;

    @FXML
    public JFXCheckBox checkBoxProcessBySteps;

    @FXML
    public ProgressIndicator progressIndicatorProcessing;

    @FXML
    public TableView table;
    @FXML
    public JFXButton btnRunAtSignLevel;
    @FXML
    public JFXTextField textSignLevel;
    @FXML
    public ProgressIndicator progressIndicatorProcessing2;

    @InjectViewModel
    private ExpViewModel viewModel;

    private boolean processBySteps = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        checkBoxProcessBySteps.selectedProperty().addListener((observableValue, oldVal, newVal) -> {
            processBySteps = newVal;
            textStep.setVisible(newVal);
        });

    }

    public void onSpustit(ActionEvent actionEvent) {
        int m = 0;
        double step = 0;
        try {
            m = Integer.parseInt(aaa.getText());
            step = Double.parseDouble(textStep.getText());
        } catch (Exception e) {
            log.error("error", e);
        }


        table.setVisible(false);
        progressIndicatorProcessing.setVisible(true);
        btnRunAtSignLevel.setVisible(false);
        textSignLevel.setVisible(false);
        if (processBySteps) {
            viewModel.processWithStep(m, step, () -> {
                progressIndicatorProcessing.setVisible(false);
                table.setVisible(true);
                btnRunAtSignLevel.setVisible(true);
                textSignLevel.setVisible(true);
                setSignificanceLevelTable();
            });
        } else {
            viewModel.processWithoutSteps(m, () -> this.progressIndicatorProcessing.setVisible(false));
        }
    }

    private void setSignificanceLevelTable() {
        ObservableList<PlaceHolder> significanceLevelList = viewModel.getSignificanceLevelList();

        table.getColumns().clear();

        int prefWidth = significanceLevelList.size();

        for(int i = 0; i < significanceLevelList.size(); i++) {
            int finalI = i;
            TableColumn<List<PlaceHolder>, String> col = HelperFunctions.createColumnWithIntComparable( significanceLevelList.get(i).getSignificanceLevel() + "",
                        TextFieldTableCell.forTableColumn(),
                        data -> new ReadOnlyStringWrapper(significanceLevelList.get(finalI).getObjectValue() + ""),
                        null,
                        prefWidth, table);

                table.getColumns().add(col);

        }

        ObservableList<List<PlaceHolder>> olist = FXCollections.observableArrayList();
        olist.add(significanceLevelList);

        table.setItems(olist);
    }

    public void onRunAtSignLevel(ActionEvent actionEvent) {
        Double m = 0.0;
        try {
            m = Double.parseDouble(btnRunAtSignLevel.getText());
        } catch (Exception e) {
            log.error("error", e);
        }

        this.progressIndicatorProcessing2.setVisible(true);
        viewModel.processAtSignLevel(m, () -> this.progressIndicatorProcessing2.setVisible(false));
    }
}
