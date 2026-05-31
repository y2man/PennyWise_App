open module com.pennywise.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires kotlin.stdlib;
    requires java.net.http;
    requires java.prefs;
    exports com.pennywise.ui;
}
