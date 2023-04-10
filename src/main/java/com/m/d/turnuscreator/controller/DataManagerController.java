package com.m.d.turnuscreator.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.m.d.turnuscreator.bean.Edge;
import com.m.d.turnuscreator.bean.Node;
import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.viewmodel.DataManagerViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Slf4j
public class DataManagerController implements FxmlView<DataManagerViewModel>, Initializable {

    private static String DELETE_BTN_STRING = "-fx-font-size: 17px; -fx-background-color: #D50000; -fx-padding: 0.35em 0.50em;";

    private static final String BOX_ZASTAVKY = "Zastavky";
    private static final String BOX_USEKY = "Useky";
    private static final String BOX_SPOJE = "Spoje";

    @FXML
    public Label labelInfoDataPath;
    @FXML
    public Label labelInfoConnections;
    @FXML
    public Label labelInfoStations;
    @FXML
    public Label labelInfoEdges;

    @FXML
    public Label labelInfoEdgesStatic;
    @FXML
    public Label labelInfoStationsStatic;
    @FXML
    public Label labelInfoConnectionsStatic;
    @FXML
    public Label labelInfoDataPathStatic;
    @FXML
    public TableView tableViewInputData;
    @FXML
    public JFXComboBox<String> comboBoxInputData;

    private Stage stage;

    @InjectViewModel
    private DataManagerViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        labelInfoDataPath.textProperty().bindBidirectional(viewModel.getTextInfoDataPath());
        labelInfoConnections.textProperty().bindBidirectional(viewModel.getTextInfoConnectionsCount());
        labelInfoStations.textProperty().bindBidirectional(viewModel.getTextInfoStationsCount());
        labelInfoEdges.textProperty().bindBidirectional(viewModel.getTextInfoEdgesCount());

        initInputDataTable();
    }

    private <S, T> TableColumn<S, T> createColumn(String colName, Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory,
                                                  Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> cellValueFactory,
                                                  EventHandler<TableColumn.CellEditEvent<S, T>> onEditCommit,
                                                  int prefWidth) {
        TableColumn<S, T> col = new TableColumn<>(colName);
        if (cellFactory != null) col.setCellFactory(cellFactory);
        if (cellValueFactory != null) col.setCellValueFactory(cellValueFactory);
        if (onEditCommit != null) col.setOnEditCommit(onEditCommit);
        col.prefWidthProperty().bind(tableViewInputData.widthProperty().divide(prefWidth));
        return col;
    }

    private <S> TableColumn<S, String> createColumnWithIntComparable(String colName, Callback<TableColumn<S, String>, TableCell<S, String>> cellFactory,
                                                                     Callback<TableColumn.CellDataFeatures<S, String>, ObservableValue<String>> cellValueFactory,
                                                                     EventHandler<TableColumn.CellEditEvent<S, String>> onEditCommit,
                                                                     int prefWidth) {
        TableColumn<S, String> col = createColumn(colName, cellFactory, cellValueFactory, onEditCommit, prefWidth);
        col.setComparator((o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;

            Integer i1 = null;
            try {
                i1 = Integer.valueOf(o1);
            } catch (NumberFormatException ignored) {
            }
            Integer i2 = null;
            try {
                i2 = Integer.valueOf(o2);
            } catch (NumberFormatException ignored) {
            }

            if (i1 == null && i2 == null) return o1.compareTo(o2);
            if (i1 == null) return -1;
            if (i2 == null) return 1;

            return i1 - i2;
        });
        return col;
    }

    public void initInputDataTable() {
        tableViewInputData.setEditable(true);

        //comboBox
        ObservableList<String> list = FXCollections.observableList(List.of(BOX_ZASTAVKY, BOX_USEKY, BOX_SPOJE));

        comboBoxInputData.setItems(FXCollections.unmodifiableObservableList(list));
        comboBoxInputData.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

            if (oldValue == null || !oldValue.toString().equals(newValue.toString())) {
                tableViewInputData.getColumns().clear();

                switch (newValue.toString()) {
                    case BOX_USEKY:
                        createEdgesTable();
                        break;
                    case BOX_ZASTAVKY:
                        createStationsTable();
                        break;
                    case BOX_SPOJE:
                        int prefWidth = 11;

                        TableColumn<Spoj, String> idCol = createColumnWithIntComparable("ID",
                                null,
                                data -> new ReadOnlyStringWrapper(data.getValue().getId() + ""),
                                null,
                                prefWidth);

                        TableColumn<Spoj, String> idLine = createColumnWithIntComparable("Linka",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getLine() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setLine(edgeStringCellEditEvent.getNewValue());
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> idSpoj = createColumnWithIntComparable("Spoj",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getSpoj() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setSpoj(edgeStringCellEditEvent.getNewValue());
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> idFromCol = createColumnWithIntComparable("Zastavka od",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getFromId() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setFromId(Integer.parseInt(edgeStringCellEditEvent.getNewValue()));
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> fromCol = createColumn("Zastavka od",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getFromName() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setFromName(edgeStringCellEditEvent.getNewValue());
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> depCol = createColumn("Odchod",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getDeparture() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setDeparture(LocalTime.parse(edgeStringCellEditEvent.getNewValue()));
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> idToCol = createColumnWithIntComparable("Zastavka do",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getToId() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setFromId(Integer.parseInt(edgeStringCellEditEvent.getNewValue()));
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> toCol = createColumn("Zastavka do",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getToName() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setFromName(edgeStringCellEditEvent.getNewValue());
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> arrCol = createColumn("Prichod",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getArrival() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setArrival(LocalTime.parse(edgeStringCellEditEvent.getNewValue()));
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, String> kmCol = createColumnWithIntComparable("km",
                                TextFieldTableCell.forTableColumn(),
                                data -> new ReadOnlyStringWrapper(data.getValue().getDistanceInKm() + ""),
                                new EventHandler<TableColumn.CellEditEvent<Spoj, String>>() {
                                    @Override
                                    public void handle(TableColumn.CellEditEvent<Spoj, String> edgeStringCellEditEvent) {
                                        edgeStringCellEditEvent.getRowValue().setDistanceInKm(Integer.parseInt(edgeStringCellEditEvent.getNewValue()));
                                    }
                                },
                                prefWidth);

                        TableColumn<Spoj, Void> delCol = createDelColumn(spoj -> viewModel.removeConnection(spoj));

                        tableViewInputData.getColumns().addAll(idCol, idLine, idSpoj, idFromCol, fromCol, depCol,
                                idToCol, toCol, arrCol,kmCol, delCol);
                        tableViewInputData.setItems(viewModel.getSpojObservableList());
                        break;
                }
            }
        });
    }

    private void createEdgesTable() {
        int prefWidth;
        prefWidth = 7;

        TableColumn<Edge, String> edgeFromIdCol = createColumnWithIntComparable("Zastavka od",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getFromId() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        edgeStringCellEditEvent.getRowValue().setFromId(Integer.parseInt(edgeStringCellEditEvent.getNewValue()));
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeFromNameCol = createColumn("Zastavka od",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getFromName() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        edgeStringCellEditEvent.getRowValue().setFromName(edgeStringCellEditEvent.getNewValue());
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeToIdCol = createColumnWithIntComparable("Zastavka do",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getToId() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        edgeStringCellEditEvent.getRowValue().setToId(Integer.parseInt(edgeStringCellEditEvent.getNewValue()));
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeToNameCol = createColumn("Zastavka do",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getToName() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        edgeStringCellEditEvent.getRowValue().setToName(edgeStringCellEditEvent.getNewValue());
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeMetrCol = createColumnWithIntComparable("Metre",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getMeters() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        edgeStringCellEditEvent.getRowValue().setMeters(Integer.parseInt(edgeStringCellEditEvent.getNewValue()));
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeSecCol1 = createColumnWithIntComparable("Sekundy L",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getSecondsTriangular().getLeft() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        Triple<Integer, Integer, Integer> oldVal = edgeStringCellEditEvent.getRowValue().getSecondsTriangular();
                        edgeStringCellEditEvent.getRowValue().setSecondsTriangular(
                                Triple.of(Integer.parseInt(edgeStringCellEditEvent.getNewValue()),
                                        oldVal.getMiddle(), oldVal.getRight()
                                ));
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeSecCol2 = createColumnWithIntComparable("Sekundy S",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getSecondsTriangular().getMiddle() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        Triple<Integer, Integer, Integer> oldVal = edgeStringCellEditEvent.getRowValue().getSecondsTriangular();
                        edgeStringCellEditEvent.getRowValue().setSecondsTriangular(
                                Triple.of(oldVal.getLeft(),
                                        Integer.parseInt(edgeStringCellEditEvent.getNewValue()), oldVal.getRight()
                                ));
                    }
                },
                prefWidth
        );

        TableColumn<Edge, String> edgeSecCol3 = createColumnWithIntComparable("Sekundy P",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getSecondsTriangular().getRight() + ""),
                new EventHandler<TableColumn.CellEditEvent<Edge, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Edge, String> edgeStringCellEditEvent) {
                        Triple<Integer, Integer, Integer> oldVal = edgeStringCellEditEvent.getRowValue().getSecondsTriangular();
                        edgeStringCellEditEvent.getRowValue().setSecondsTriangular(
                                Triple.of(oldVal.getLeft(),
                                        oldVal.getMiddle(), Integer.parseInt(edgeStringCellEditEvent.getNewValue())
                                ));
                    }
                },
                prefWidth
        );

        TableColumn<Edge, Void> delCol = createDelColumn(edge -> viewModel.removeEdge(edge));

        tableViewInputData.getColumns().addAll(edgeFromIdCol, edgeFromNameCol, edgeToIdCol, edgeToNameCol, edgeMetrCol,
                edgeSecCol1, edgeSecCol2, edgeSecCol3, delCol);
        tableViewInputData.setItems(viewModel.getEdgeObservableList());
    }

    private void createStationsTable() {
        int prefWidth;
        prefWidth = 3;

        TableColumn<Node, String> idCol = createColumnWithIntComparable("ID",
                null,
                data -> new ReadOnlyStringWrapper(data.getValue().getId() + ""),
                null,
                prefWidth);

        TableColumn<Node, String> nameCol = createColumn("Zastavka",
                TextFieldTableCell.forTableColumn(),
                data -> new ReadOnlyStringWrapper(data.getValue().getName() + ""),
                new EventHandler<TableColumn.CellEditEvent<Node, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Node, String> nodeStringCellEditEvent) {
                        nodeStringCellEditEvent.getRowValue().setName(nodeStringCellEditEvent.getNewValue());
                    }
                },
                prefWidth
        );

        TableColumn<Node, Void> delCol1 = createDelColumn(node -> viewModel.removeNode(node));

        tableViewInputData.getColumns().addAll(idCol, nameCol, delCol1);
        tableViewInputData.setItems(viewModel.getNodeObservableList());
    }

    private <S> TableColumn<S, Void> createDelColumn(Consumer<S> fun) {
        TableColumn<S, Void> delCol = new TableColumn("");
        delCol.setPrefWidth(100);
        Callback<TableColumn<S, Void>, TableCell<S, Void>> cellFactory = new Callback<TableColumn<S, Void>, TableCell<S, Void>>() {
            @Override
            public TableCell<S, Void> call(final TableColumn<S, Void> param) {
                final TableCell<S, Void> cell = new TableCell<S, Void>() {

                    private final JFXButton btn = new JFXButton("Vymaz");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            S data = getTableView().getItems().get(getIndex());
                            log.info("Deleting node: {}", data);
                            fun.accept(data);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            btn.setStyle(DELETE_BTN_STRING);
                            btn.setMaxHeight(2.0);
                            btn.setPrefWidth(100.0);
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        delCol.setCellFactory(cellFactory);

        return delCol;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void onLoadData(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File path = directoryChooser.showDialog(stage);
        if (path != null) {
            log.info("Load data from {}", path.getAbsolutePath());
            if (viewModel.loadDataFromPath(path.getAbsolutePath())) {
                labelInfoDataPath.setVisible(true);
                labelInfoConnections.setVisible(true);
                labelInfoStations.setVisible(true);
                labelInfoEdges.setVisible(true);

                labelInfoStationsStatic.setVisible(true);
                labelInfoEdgesStatic.setVisible(true);
                labelInfoDataPathStatic.setVisible(true);
                labelInfoConnectionsStatic.setVisible(true);
            }
        }
    }

    public void addNewData(ActionEvent actionEvent) {
        String selectedItem = comboBoxInputData.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            switch (selectedItem) {
                case BOX_ZASTAVKY:
                    viewModel.createNewNode();
                    Platform.runLater(() -> tableViewInputData.scrollTo(viewModel.getNodeObservableList().size() - 1));
                    break;
                case BOX_SPOJE:
                    viewModel.createNewConnection();
                    Platform.runLater(() -> tableViewInputData.scrollTo(viewModel.getSpojObservableList().size() - 1));
                    break;
                case BOX_USEKY:
                    viewModel.createNewEdge();
                    Platform.runLater(() -> tableViewInputData.scrollTo(viewModel.getEdgeObservableList().size() - 1));
                    break;
            }
        }
    }

    public void onCancelEdit(ActionEvent actionEvent) {
        viewModel.reloadData();
    }

    public void onSaveData(ActionEvent actionEvent) {
        viewModel.saveData();
    }
}
