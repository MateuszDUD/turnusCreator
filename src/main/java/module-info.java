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
    requires org.apache.commons.lang3;
    requires org.apache.pdfbox;
//    requires artifact.gurobi;
    requires gurobi;
    requires java.desktop;

    opens com.m.d.turnuscreator to de.saxsys.mvvmfx, javafx.fxml;
    opens com.m.d.turnuscreator.controller to de.saxsys.mvvmfx, javafx.fxml;
    opens com.m.d.turnuscreator.viewmodel to de.saxsys.mvvmfx, javafx.fxml;
    exports com.m.d.turnuscreator;
}