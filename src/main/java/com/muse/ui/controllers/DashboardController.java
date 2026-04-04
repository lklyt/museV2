package com.muse.ui.controllers;

import com.muse.service.PostService;
import com.muse.service.CommunityService;
import com.muse.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final PostService postService = new PostService();
    private final CommunityService communityService = new CommunityService();

    @FXML private Label userLabel;
    @FXML private Button logoutButton;
    @FXML private Button homeButton;
    @FXML private Button communitiesButton;
    @FXML private Button myPostsButton;
    @FXML private Button profileButton;
    @FXML private Button createPostButton;
    @FXML private Button createCommunityButton;
    @FXML private Label titleLabel;
    @FXML private VBox contentVBox;

    @FXML
    public void initialize() {
        var currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Welcome, " + currentUser.getUsername());
        }

        logoutButton.setOnAction(e -> handleLogout());
        homeButton.setOnAction(e -> loadHome());
        communitiesButton.setOnAction(e -> loadCommunities());
        myPostsButton.setOnAction(e -> loadMyPosts());
        profileButton.setOnAction(e -> loadProfile());
        createPostButton.setOnAction(e -> handleCreatePost());
        createCommunityButton.setOnAction(e -> handleCreateCommunity());

        loadHome();
    }

    private void loadHome() {
        titleLabel.setText("Feed");
        contentVBox.getChildren().clear();

        try {
            var posts = postService.getAllPosts();
            for (var post : posts) {
                Label postLabel = new Label(post.getTitle() + " by " + post.getAuthorUsername());
                postLabel.setWrapText(true);
                contentVBox.getChildren().add(postLabel);
            }

            if (posts.isEmpty()) {
                contentVBox.getChildren().add(new Label("No posts yet. Be the first to post!"));
            }
        } catch (Exception e) {
            logger.error("Error loading posts", e);
            contentVBox.getChildren().add(new Label("Error loading posts: " + e.getMessage()));
        }
    }

    private void loadCommunities() {
        titleLabel.setText("Communities");
        contentVBox.getChildren().clear();

        try {
            var communities = communityService.getAllCommunities();
            for (var community : communities) {
                Label commLabel = new Label(community.getName() + " (" + community.getMemberCount() + " members)");
                commLabel.setWrapText(true);
                contentVBox.getChildren().add(commLabel);
            }

            if (communities.isEmpty()) {
                contentVBox.getChildren().add(new Label("No communities found."));
            }
        } catch (Exception e) {
            logger.error("Error loading communities", e);
            contentVBox.getChildren().add(new Label("Error loading communities: " + e.getMessage()));
        }
    }

    private void loadMyPosts() {
        titleLabel.setText("My Posts");
        contentVBox.getChildren().clear();

        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            var posts = postService.getPostsByAuthor(userId);
            for (var post : posts) {
                Label postLabel = new Label(post.getTitle());
                postLabel.setWrapText(true);
                contentVBox.getChildren().add(postLabel);
            }

            if (posts.isEmpty()) {
                contentVBox.getChildren().add(new Label("You haven't posted anything yet."));
            }
        } catch (Exception e) {
            logger.error("Error loading user posts", e);
            contentVBox.getChildren().add(new Label("Error loading posts: " + e.getMessage()));
        }
    }

    private void loadProfile() {
        titleLabel.setText("Profile");
        contentVBox.getChildren().clear();

        var currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            contentVBox.getChildren().add(new Label("Username: " + currentUser.getUsername()));
            contentVBox.getChildren().add(new Label("Email: " + currentUser.getEmail()));
            contentVBox.getChildren().add(new Label("Display Name: " + currentUser.getDisplayName()));
        }
    }

    @FXML
    private void handleCreatePost() {
        // TODO: Open create post dialog
        logger.info("Create post clicked");
    }

    @FXML
    private void handleCreateCommunity() {
        // TODO: Open create community dialog
        logger.info("Create community clicked");
    }

    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 600, 500));
        } catch (Exception e) {
            logger.error("Error loading login view", e);
        }
    }
}
