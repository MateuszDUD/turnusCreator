package com.m.d.turnuscreator.controller;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class HelperFunctions {

    public static <S, T> TableColumn<S, T> createColumn(String colName, Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory,
                                                  Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> cellValueFactory,
                                                  EventHandler<TableColumn.CellEditEvent<S, T>> onEditCommit,
                                                  int prefWidth, TableView tableView) {
        TableColumn<S, T> col = new TableColumn<>(colName);
        if (cellFactory != null) col.setCellFactory(cellFactory);
        if (cellValueFactory != null) col.setCellValueFactory(cellValueFactory);
        if (onEditCommit != null) col.setOnEditCommit(onEditCommit);
        col.prefWidthProperty().bind(tableView.widthProperty().divide(prefWidth));
        return col;
    }

    public static <S> TableColumn<S, String> createColumnWithIntComparable(String colName, Callback<TableColumn<S, String>, TableCell<S, String>> cellFactory,
                                                                     Callback<TableColumn.CellDataFeatures<S, String>, ObservableValue<String>> cellValueFactory,
                                                                     EventHandler<TableColumn.CellEditEvent<S, String>> onEditCommit,
                                                                     int prefWidth, TableView tableView) {
        TableColumn<S, String> col = createColumn(colName, cellFactory, cellValueFactory, onEditCommit, prefWidth, tableView);
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
}
