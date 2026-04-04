package com.muse.ui.controllers;

import com.muse.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final UserService userService = new UserService();

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField displayNameField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> handleRegister());
        backButton.setOnAction(e -> handleBackToLogin());
        errorLabel.setText("");
        successLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String displayName = displayNameField.getText();

        // Clear previous messages
        errorLabel.setText("");
        successLabel.setText("");

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            errorLabel.setText("All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        try {
            var userOpt = userService.register(username, email, password, displayName);
            if (userOpt.isPresent()) {
                successLabel.setText("Registration successful! Redirecting to login...");
                // Redirect to login after 2 seconds
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(2000);
                        handleBackToLogin();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            logger.error("Registration error", e);
            errorLabel.setText("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 600, 500));
        } catch (Exception e) {
            logger.error("Error loading login view", e);
        }
    }
}
