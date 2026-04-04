package com.muse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.muse.config.DatabaseConfig;

public class MuseApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MuseApp.class);
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting MUSE Application");

            // Load login view as initial screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setTitle("MUSE - Social Network");
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        try {
            logger.info("Closing MUSE Application");
            DatabaseConfig.closeDataSource();
        } catch (Exception e) {
            logger.error("Error closing application", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
