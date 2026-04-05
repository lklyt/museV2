package com.muse.ui.controllers;

import com.muse.service.UserService;
import com.muse.util.SessionManager;
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
 * Controller for login.fxml.
 *
 * <p>The login FXML labels the first field "Email" but uses {@code fx:id="usernameField"}.
 * This controller accepts either an e-mail address or a plain username and delegates
 * the look-up to {@link UserService#login(String, String)}.
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final UserService userService = new UserService();

    // ── FXML bindings ────────────────────────────────────────────────────────
    /** Accepts either an e-mail address or a username (label in FXML reads "Email"). */
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    /** Small error label injected below the input fields in the updated FXML. */
    @FXML private Label errorLabel;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        errorLabel.setText("");
        // onAction attributes in FXML wire #handleLogin / #handleRegisterClick directly;
        // the setOnAction calls below provide a programmatic fallback for older FXMLs.
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegisterClick());
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    @FXML
    private void handleLogin() {
        String identifier = usernameField.getText().trim();
        String password   = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Please enter your e-mail (or username) and password.");
            return;
        }

        try {
            var userOpt = userService.login(identifier, password);
            if (userOpt.isPresent()) {
                SessionManager.getInstance().login(userOpt.get());
                loadDashboard();
            } else {
                showError("Invalid credentials. Please try again.");
            }
        } catch (Exception ex) {
            logger.error("Login error", ex);
            showError("Login failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRegisterClick() {
        navigateTo("/views/register.fxml", 900, 700);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void loadDashboard() {
        navigateTo("/views/dashboard.fxml", 1200, 800);
    }

    /**
     * Replaces the current scene with the FXML at {@code fxmlPath}.
     *
     * @param fxmlPath   classpath-relative path (e.g. {@code "/views/dashboard.fxml"})
     * @param width      preferred scene width
     * @param height     preferred scene height
     */
    private void navigateTo(String fxmlPath, double width, double height) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (Exception ex) {
            logger.error("Navigation error – could not load {}", fxmlPath, ex);
            showError("Navigation error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
