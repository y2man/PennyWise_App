package com.pennywise.ui.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.pennywise.ui.PennyWiseApp;
import com.pennywise.ui.service.ApiClient;
import com.pennywise.ui.util.SessionStore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Map;
import java.util.concurrent.Executors;

public class LoginController {

    @FXML
    private VBox loginPane, forgotPane, verifyPane, resetPane;
    @FXML
    private TextField loginEmail, forgotEmail;
    @FXML
    private PasswordField loginPassword, newPassword, confirmPassword;
    @FXML
    private Label loginError, forgotError, forgotSuccess, otpError, resetError, strengthLabel, sentToLabel;
    @FXML
    private TextField otp0, otp1, otp2, otp3, otp4, otp5;
    @FXML
    private ProgressBar strengthBar;

    private String _otpEmail, _resetToken;

    @FXML
    public void initialize() {
        showPane("login");
        TextField[] boxes = {otp0, otp1, otp2, otp3, otp4, otp5};
        for (int i = 0; i < boxes.length; i++) {
            final int idx = i;
            boxes[i].textProperty().addListener((obs, o, n) -> {
                if (n.length() > 1) {
                    boxes[idx].setText(n.substring(n.length() - 1));
                }
                if (!n.isEmpty() && idx < 5) {
                    boxes[idx + 1].requestFocus();
                }
            });
        }
        newPassword.textProperty().addListener((obs, o, n) -> updateStrength(n));
    }

    private void showPane(String name) {
        for (VBox p : new VBox[]{loginPane, forgotPane, verifyPane, resetPane}) {
            if (p != null) {
                p.setVisible(false);
                p.setManaged(false);
            }
        }
        VBox t = switch (name) {
            case "forgot" ->
                forgotPane;
            case "verify" ->
                verifyPane;
            case "reset" ->
                resetPane;
            default ->
                loginPane;
        };
        if (t != null) {
            t.setVisible(true);
            t.setManaged(true);
        }
    }

    private void clearLabels() {
        for (Label l : new Label[]{loginError, forgotError, forgotSuccess, otpError, resetError}) {
            if (l != null) {
                l.setText("");
    
            }
        }}

    @FXML
    public void goToForgot() {
        clearLabels();
        showPane("forgot");
    }

    @FXML
    public void goToLogin() {
        clearLabels();
        showPane("login");
    }

    @FXML
    public void goToSignup() {
        PennyWiseApp.navigateTo("signup");
    }

    @FXML
    public void handleLogin() {
        loginError.setText("");
        String em = loginEmail.getText().trim(), pw = loginPassword.getText();
        if (em.isEmpty() || pw.isEmpty()) {
            loginError.setText("Please fill all fields");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonNode res = ApiClient.postNoAuth("/auth/login", Map.of("email", em, "password", pw));
                SessionStore.setToken(res.get("token").asText());
                ApiClient.setAuthToken(SessionStore.getToken());
                SessionStore.setEmail(em);
                SessionStore.setName(res.has("name") ? res.get("name").asText() : em.split("@")[0]);
                Platform.runLater(() -> PennyWiseApp.navigateTo("dashboard"));
            } catch (Exception e) {
                Platform.runLater(() -> loginError.setText("Invalid email or password"));
            }
        });
    }

    @FXML
    public void handleForgot() {
        forgotError.setText("");
        forgotSuccess.setText("");
        String em = forgotEmail.getText().trim();
        if (!em.contains("@")) {
            forgotError.setText("Enter a valid email");
            return;
        }
        _otpEmail = em;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.postNoAuth("/auth/forgot-password", Map.of("email", em));
                Platform.runLater(() -> {
                    if (sentToLabel != null) {
                        sentToLabel.setText("Code sent to " + em);
                    }
                    for (TextField b : new TextField[]{otp0, otp1, otp2, otp3, otp4, otp5}) {
                        if (b != null) {
                            b.clear();
                        }
                    }
                    showPane("verify");
                    if (otp0 != null) {
                        otp0.requestFocus();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> forgotError.setText("Failed to send code"));
            }
        });
    }

    @FXML
    public void handleResend() {
        handleForgot();
    }

    @FXML
    public void handleVerify() {
        if (otpError != null) {
            otpError.setText("");
        }
        String code = (otp0 != null ? otp0.getText() : "") + (otp1 != null ? otp1.getText() : "") + (otp2 != null ? otp2.getText() : "")
                + (otp3 != null ? otp3.getText() : "") + (otp4 != null ? otp4.getText() : "") + (otp5 != null ? otp5.getText() : "");
        if (code.length() < 6) {
            if (otpError != null) {
                otpError.setText("Enter all 6 digits");
            
            }return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonNode res = ApiClient.postNoAuth("/auth/verify-otp", Map.of("email", _otpEmail, "code", code));
                _resetToken = res.get("resetToken").asText();
                Platform.runLater(() -> showPane("reset"));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (otpError != null) {
                        otpError.setText("Incorrect or expired code");
                    }
                    for (TextField b : new TextField[]{otp0, otp1, otp2, otp3, otp4, otp5}) {
                        if (b != null) {
                            b.clear();
                        }
                    }
                    if (otp0 != null) {
                        otp0.requestFocus();
                    }
                });
            }
        });
    }

    @FXML
    public void handleReset() {
        if (resetError != null) {
            resetError.setText("");
        }
        String pw1 = newPassword.getText(), pw2 = confirmPassword.getText();
        if (pw1.length() < 6) {
            if (resetError != null) {
                resetError.setText("Minimum 6 characters");
            
            }return;
        }
        if (!pw1.equals(pw2)) {
            if (resetError != null) {
                resetError.setText("Passwords do not match");
            
            }return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.postNoAuth("/auth/reset-password", Map.of("resetToken", _resetToken, "newPassword", pw1));
                Platform.runLater(() -> {
                    showPane("login");
                    if (loginError != null) {
                        loginError.setStyle("-fx-text-fill:#5cba8a;");
                        loginError.setText("Password updated — please sign in");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (resetError != null) {
                        resetError.setText("Reset failed. Try again.");
                
                    }});
            }
        });
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
