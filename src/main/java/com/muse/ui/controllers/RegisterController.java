package com.muse.ui.controllers;

import com.muse.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for register.fxml.
 *
 * <p>The updated FXML collects: email, username, password, confirm-password.
 * {@code displayName} is seeded from the username because the new design
 * no longer includes a separate display-name field.
 */
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final UserService userService = new UserService();

    // ── FXML bindings ────────────────────────────────────────────────────────
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        errorLabel.setText("");
        successLabel.setText("");
        // onAction attributes in the FXML wire #handleRegister / #handleBackToLogin;
        // the setOnAction calls below act as a safe fallback.
        registerButton.setOnAction(e -> handleRegister());
        backButton.setOnAction(e -> handleBackToLogin());
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    @FXML
    private void handleRegister() {
        clearMessages();

        String email           = emailField.getText().trim();
        String username        = usernameField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // ── Validation ──────────────────────────────────────────────────────
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        // ── Registration ─────────────────────────────────────────────────────
        try {
            // displayName falls back to username since the new UI has no separate field
            var userOpt = userService.register(username, email, password, username);
            if (userOpt.isPresent()) {
                showSuccess("Account created! Redirecting to login…");
                registerButton.setDisable(true);
                // Delay redirect so the user can read the success message
                new Thread(() -> {
                    try {
                        Thread.sleep(1_800);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(this::handleBackToLogin);
                }).start();
            } else {
                showError("Registration failed. Username or e-mail may already be in use.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Registration error", ex);
            showError("Registration failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        navigateTo("/views/login.fxml", 900, 600);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Replaces the current scene with the FXML at {@code fxmlPath}.
     */
    private void navigateTo(String fxmlPath, double width, double height) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (Exception ex) {
            logger.error("Navigation error – could not load {}", fxmlPath, ex);
            showError("Navigation error: " + ex.getMessage());
        }
    }

    private void clearMessages() {
        errorLabel.setText("");
        successLabel.setText("");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        successLabel.setText("");
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        errorLabel.setText("");
    }
}
