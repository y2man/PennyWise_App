package com.pennywise.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class PennyWiseApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("PennyWise");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        navigateTo("login");
        stage.show();
    }

    /**
     * Navigate to a named view. MUST be called on the JavaFX Application Thread.
     * If called from a background thread, use Platform.runLater(() -> navigateTo(...)).
     */
    public static void navigateTo(String view) {
        // Ensure we're on the FX thread — if not, dispatch and return
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> navigateTo(view));
            return;
        }

        try {
            URL fxmlUrl = PennyWiseApp.class.getResource(
                    "/com/pennywise/ui/views/" + view + ".fxml");
            if (fxmlUrl == null) {
                showError("FXML Not Found",
                    "Cannot find: /com/pennywise/ui/views/" + view + ".fxml\n\n" +
                    "Make sure the file exists in src/main/resources.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());

            URL cssUrl = PennyWiseApp.class.getResource("/styles/app.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            primaryStage.setScene(scene);

            if (view.equals("dashboard")) {
                primaryStage.setWidth(1280);
                primaryStage.setHeight(800);
            } else {
                primaryStage.setWidth(920);
                primaryStage.setHeight(620);
            }
            primaryStage.centerOnScreen();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            Throwable t = e;
            while (t != null) {
                t.printStackTrace(pw);
                t = t.getCause();
                if (t != null) pw.println("\n>>> Caused by >>>");
            }
            showError("Failed to load view: " + view, sw.toString());
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PennyWise Error");
        alert.setHeaderText(title);
        TextArea ta = new TextArea(message);
        ta.setEditable(false);
        ta.setWrapText(false);
        ta.setPrefHeight(400);
        ta.setPrefWidth(750);
        VBox box = new VBox(ta);
        alert.getDialogPane().setContent(box);
        alert.getDialogPane().setPrefWidth(800);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
