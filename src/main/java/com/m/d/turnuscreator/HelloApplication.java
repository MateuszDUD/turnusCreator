package com.m.d.turnuscreator;

import com.m.d.turnuscreator.controller.DataManagerController;
import com.m.d.turnuscreator.controller.MainController;
import com.m.d.turnuscreator.viewmodel.DataManagerViewModel;
import com.m.d.turnuscreator.viewmodel.MainViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private ViewTuple<MainController, MainViewModel> viewTuple;

    @Override
    public void start(Stage stage) throws IOException {
//        stage.setTitle("Hello Hell :)");

        ViewTuple<MainController, MainViewModel> viewTuple = FluentViewLoader.fxmlView(MainController.class).load();

        Parent root = viewTuple.getView();

//        (viewTuple.getCodeBehind()).setStage(stage);

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