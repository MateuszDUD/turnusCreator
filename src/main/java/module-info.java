module com.m.d.turnuscreator {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires static org.mapstruct.processor;
    requires com.jfoenix;
    requires de.saxsys.mvvmfx;
    requires opencsv;
    requires org.slf4j;
    requires rxjavafx;
    requires io.reactivex.rxjava2;
    requires java.sql;

    opens com.m.d.turnuscreator to de.saxsys.mvvmfx, javafx.fxml;
    opens com.m.d.turnuscreator.view to de.saxsys.mvvmfx, javafx.fxml;
    opens com.m.d.turnuscreator.viewmodel to de.saxsys.mvvmfx, javafx.fxml;
    //opens com.sun.javafx.scene.control.behavior to com.jfoenix;
    exports com.m.d.turnuscreator;
}