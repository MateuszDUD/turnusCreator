package com.m.d.turnuscreator.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.m.d.turnuscreator.bean.SatisfactionLevel;
import com.m.d.turnuscreator.bean.Stop;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import com.m.d.turnuscreator.viewmodel.SolverViewModel;
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

@Slf4j
public class SolverController implements FxmlView<SolverViewModel>, Initializable {
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
    @FXML
    public JFXComboBox comboBoxDepo;

    @InjectViewModel
    private SolverViewModel viewModel;

    private BaseDataRepository repository = BaseDataRepository.getInstance();

    private boolean processBySteps = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        checkBoxProcessBySteps.selectedProperty().addListener((observableValue, oldVal, newVal) -> {
            processBySteps = newVal;
            textStep.setVisible(newVal);
        });

        comboBoxDepo.setItems(repository.getStopObservableList());
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

        Object o = comboBoxDepo.getSelectionModel().getSelectedItem();
        Stop depo = null;
        if (o instanceof Stop) {
            depo = (Stop) o;
        }

        if (depo == null) {
            log.warn("no depo selected");
            return;
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
            viewModel.processWithoutSteps(depo, m, () -> this.progressIndicatorProcessing.setVisible(false));
        }
    }

    private void setSignificanceLevelTable() {
        ObservableList<SatisfactionLevel> satisfactionLevelList = viewModel.getSignificanceLevelList();

        table.getColumns().clear();

        int prefWidth = satisfactionLevelList.size();

        int schedulesCount = viewModel.getScheduleCount();

        for (int i = 0; i < satisfactionLevelList.size(); i++) {
            int finalI = i;
            TableColumn<List<SatisfactionLevel>, String> col = HelperFunctions.createColumnWithIntComparable(satisfactionLevelList.get(i).getSatisfactionLevelValue() + "",
                    TextFieldTableCell.forTableColumn(),
                    data -> new ReadOnlyStringWrapper(schedulesCount - satisfactionLevelList.get(finalI).getObjectValue() + ""),
                    null,
                    prefWidth, table);

            table.getColumns().add(col);

        }

        ObservableList<List<SatisfactionLevel>> olist = FXCollections.observableArrayList();
        olist.add(satisfactionLevelList);

        table.setItems(olist);
    }

    public void onRunAtSignLevel(ActionEvent actionEvent) {
        Double m = 0.0;
        try {
            m = Double.parseDouble(textSignLevel.getText());
        } catch (Exception e) {
            log.error("error", e);
        }

        Object o = comboBoxDepo.getSelectionModel().getSelectedItem();

        if (o instanceof Stop) {
            Stop depo = (Stop) o;

            this.progressIndicatorProcessing2.setVisible(true);
            viewModel.processAtSignLevel(depo, m, () -> this.progressIndicatorProcessing2.setVisible(false));
        }
    }
}
