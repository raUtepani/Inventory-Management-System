module org.example.javafx
{
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires javafx.web;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens org.example.javafx to javafx.fxml;
    exports org.example.javafx;
    opens clientside to javafx.fxml;
    exports clientside;
    opens serverside to javafx.fxml;
    exports serverside;

    exports practice;

}