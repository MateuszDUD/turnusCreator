package com.m.d.turnuscreator;

import com.m.d.turnuscreator.view.MainView;
import com.m.d.turnuscreator.viewmodel.MainViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private ViewTuple<MainView,MainViewModel> viewTuple;

    @Override
    public void start(Stage stage) throws IOException {
//        stage.setTitle("Hello Hell :)");

        ViewTuple<MainView, MainViewModel> viewTuple = FluentViewLoader.fxmlView(MainView.class).load();

        Parent root = viewTuple.getView();

        (viewTuple.getCodeBehind()).setStage(stage);

        this.viewTuple = viewTuple;

        stage.setWidth(1200);
        stage.setHeight(900.0);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}