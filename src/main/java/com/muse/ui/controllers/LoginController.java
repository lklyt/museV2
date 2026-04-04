package com.muse.ui.controllers;

import com.muse.service.UserService;
import com.muse.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final UserService userService = new UserService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegisterClick());
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        try {
            var userOpt = userService.login(username, password);
            if (userOpt.isPresent()) {
                SessionManager.getInstance().login(userOpt.get());
                loadMainDashboard();
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            errorLabel.setText("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterClick() {
        try {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/register.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 600, 500));
        } catch (Exception e) {
            logger.error("Error loading register view", e);
        }
    }

    private void loadMainDashboard() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 1200, 800));
        } catch (Exception e) {
            logger.error("Error loading dashboard", e);
        }
    }
}
