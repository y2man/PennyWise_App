package com.pennywise.ui.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.pennywise.ui.PennyWiseApp;
import com.pennywise.ui.service.ApiClient;
import com.pennywise.ui.util.SessionStore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Map;
import java.util.concurrent.Executors;

public class SignupController {

    @FXML
    private TextField nameField, emailField;
    @FXML
    private PasswordField passwordField, confirmField;
    @FXML
    private ProgressBar strengthBar;
    @FXML
    private Label strengthLabel, errorLabel, successLabel;

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, o, n) -> updateStrength(n));
    }

    @FXML
    public void handleSignup() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        String name = nameField.getText().trim(), email = emailField.getText().trim();
        String pw = passwordField.getText(), confirm = confirmField.getText();
        if (name.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Enter your name");
            
            }return;
        }
        if (!email.contains("@")) {
            if (errorLabel != null) {
                errorLabel.setText("Enter a valid email");
            
            }return;
        }
        if (pw.length() < 6) {
            if (errorLabel != null) {
                errorLabel.setText("Password must be at least 6 chars");
            
            }return;
        }
        if (!pw.equals(confirm)) {
            if (errorLabel != null) {
                errorLabel.setText("Passwords do not match");
            
            }return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonNode res = ApiClient.postNoAuth("/auth/register", Map.of("name", name, "email", email, "password", pw));
                SessionStore.setToken(res.get("token").asText());
                ApiClient.setAuthToken(SessionStore.getToken());
                SessionStore.setEmail(email);
                SessionStore.setName(res.has("name") ? res.get("name").asText() : name);
                Platform.runLater(() -> PennyWiseApp.navigateTo("dashboard"));
            } catch (Exception e) {
                String msg = e.getMessage() != null && e.getMessage().contains("400") ? "Email already registered" : "Registration failed. Try again.";
                Platform.runLater(() -> {
                    if (errorLabel != null) {
                        errorLabel.setText(msg);
                
                    }});
            }
        });
    }

    @FXML
    public void goToLogin() {
        PennyWiseApp.navigateTo("login");
    }

    private void updateStrength(String pw) {
        int score = 0;
        if (pw.length() >= 6) {
            score++;
        
        }if (pw.length() >= 10) {
            score++;
        }
        if (pw.matches(".*[A-Z].*")) {
            score++;
        
        }if (pw.matches(".*[0-9].*")) {
            score++;
        }
        if (pw.matches(".*[^A-Za-z0-9].*")) {
            score++;
        }
        if (strengthBar != null) {
            strengthBar.setProgress(pw.isEmpty() ? 0 : score / 5.0);
        }
        String[] labels = {"", "Very weak", "Weak", "Fair", "Strong", "Very strong"};
        String[] colors = {"", "#e05c5c", "#e8a44a", "#e8c97a", "#8aae5c", "#5cba8a"};
        int idx = pw.isEmpty() ? 0 : Math.max(1, score);
        if (strengthLabel != null) {
            strengthLabel.setText(labels[idx]);
            strengthLabel.setStyle("-fx-text-fill:" + colors[idx] + ";");
        }
        if (strengthBar != null) {
            strengthBar.setStyle("-fx-accent:" + (pw.isEmpty() ? "#333" : colors[idx]) + ";");
        }
    }
}
