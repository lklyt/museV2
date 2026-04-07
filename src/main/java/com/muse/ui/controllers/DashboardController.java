package com.muse.ui.controllers;

import com.muse.dao.CommunityDAOImpl;
import com.muse.dao.PostDAOImpl;
import com.muse.dao.UserDAOImpl;
import com.muse.models.ClothingCategory;
import com.muse.models.ClothingItem;
import com.muse.models.Community;
import com.muse.models.Post;
import com.muse.models.SearchResult;
import com.muse.models.SearchType;
import com.muse.models.User;
import com.muse.service.ClothingItemService;
import com.muse.service.CommunityService;
import com.muse.service.PostService;
import com.muse.service.SearchService;
import com.muse.service.UserService;
import com.muse.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    // ── Services ─────────────────────────────────────────────────────────────
    private final PostService postService = new PostService();
    private final CommunityService communityService = new CommunityService();
    private final UserService userService = new UserService();
    private final ClothingItemService clothingItemService = new ClothingItemService();

    /**
     * Shared search service — used for both the global sidebar search and
     * real-time filtering in the Create Style clothing grid.
     */
    private SearchService searchService;

    // ── Create Style state ────────────────────────────────────────────────────
    /**
     * The category currently selected in Create Style. Null = no filter (show all).
     */
    private ClothingCategory currentCategory = null;

    /**
     * Full clothing item list, loaded once on initialize so that search and
     * category filtering can work together without repeated DAO calls.
     */
    private ArrayList<ClothingItem> allClothingItems = new ArrayList<>();

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Button homeButton;
    @FXML private Button communitiesButton;
    @FXML private Button createStyleButton;
    @FXML private Button profileButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;

    // ── View containers ───────────────────────────────────────────────────────
    @FXML private VBox homeView;
    @FXML private VBox communitiesView;
    @FXML private VBox createStyleView;
    @FXML private VBox profileView;
    @FXML private VBox settingsView;
    @FXML private VBox communityDetailView;
    @FXML private VBox otherProfileView;

    // ── Home view ─────────────────────────────────────────────────────────────
    @FXML private Button forYouButton;
    @FXML private Button discoverButton;
    @FXML private Button luckyButton;
    @FXML private VBox feedVBox;

    // ── Communities view ──────────────────────────────────────────────────────
    @FXML private GridPane communityGrid;
    @FXML private Button createCommunityButton;

    // ── Create-Style view ─────────────────────────────────────────────────────
    @FXML private ImageView outfitPreview;
    @FXML private Button postStyleButton;
    @FXML private GridPane clothingItemsGrid;
    @FXML private Button hatButton;
    @FXML private Button topButton;
    @FXML private Button dressButton;
    @FXML private Button coatButton;
    @FXML private Button purseButton;
    @FXML private Button bottomButton;
    @FXML private Button shoesButton;

    // ── Own-profile view ──────────────────────────────────────────────────────
    @FXML private Label usernameLabel;
    @FXML private Button followersButton;
    @FXML private Button followingButton;
    @FXML private VBox savedOutfitsVBox;
    @FXML private VBox myPostsVBox;
    @FXML private VBox followersListView;
    @FXML private VBox followersListVBox;
    @FXML private VBox followingListView;
    @FXML private VBox followingListVBox;

    // ── Settings view ─────────────────────────────────────────────────────────
    @FXML private Button publicButton;
    @FXML private Button privateButton;
    @FXML private Button blockedProfilesButton;
    @FXML private Button explicitFilterToggle;
    @FXML private Button contactSupportButton;
    @FXML private Button resetProfileButton;

    // ── Community-detail view ────────────────────────────────────────────────
    @FXML private Label communityNameLabel;
    @FXML private VBox communityPostsVBox;
    @FXML private Button addPostToCommunityButton;

    // ── Other-profile view ───────────────────────────────────────────────────
    @FXML private Label otherUsernameLabel;
    @FXML private Button followUserButton;
    @FXML private VBox otherPostsVBox;

    // ── Style constants ───────────────────────────────────────────────────────
    private static final String NAV_ACTIVE =
            "-fx-background-color: #8c9c76; -fx-text-fill: white; " +
            "-fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 16px;";
    private static final String NAV_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 16px;";
    private static final String TAB_ACTIVE =
            "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-background-radius: 15;";
    private static final String TAB_INACTIVE =
            "-fx-background-color: transparent; -fx-border-color: #8c9c76; -fx-border-radius: 15;";
    private static final String CATEGORY_SELECTED =
            "-fx-background-color: #745a42; -fx-text-fill: white; -fx-background-radius: 15;";
    private static final String CATEGORY_UNSELECTED =
            "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-background-radius: 15;";

    private boolean isForYouActive = true;
    private int currentCommunityId = -1;
    private int currentOtherUserId = -1;
    private boolean explicitFilterOn = true;
    private boolean profileIsPublic = true;

    // ═════════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        homeButton.setOnAction(e -> openHome());
        communitiesButton.setOnAction(e -> openCommunities());
        createStyleButton.setOnAction(e -> openCreateStyle());
        profileButton.setOnAction(e -> openProfile());
        settingsButton.setOnAction(e -> openSettings());
        logoutButton.setOnAction(e -> handleLogout());
        forYouButton.setOnAction(e -> openForYou());
        discoverButton.setOnAction(e -> openDiscover());
        luckyButton.setOnAction(e -> openLucky());

        // Build the shared SearchService and pre-load all clothing items into it.
        // Loading once here means every keystroke in Create Style doesn't hit the DB.
        searchService = new SearchService(
                new UserDAOImpl(),
                new CommunityDAOImpl(),
                new PostDAOImpl()
        );
        try {
            for (ClothingCategory cat : ClothingCategory.values()) {
                allClothingItems.addAll(clothingItemService.getItemsWithCachedImages(cat));
            }
            searchService.setClothingItems(allClothingItems);
        } catch (Exception ex) {
            logger.warn("Could not pre-load clothing items for search", ex);
        }

        // Real-time listener on the shared search field.
        // When Create Style is the active view, every keystroke filters the
        // clothing grid (category + text combined). In all other views the field
        // is left alone — pressing Enter triggers handleSearch() instead.
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (createStyleView.isVisible()) {
                filterClothingGrid(newVal);
            }
        });

        openHome();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Navigation – sidebar
    // ═════════════════════════════════════════════════════════════════════════

    @FXML public void openHome() {
        activateView(homeView);
        setNavActive(homeButton);
        if (isForYouActive) openForYou(); else openDiscover();
    }

    @FXML public void openCommunities() {
        activateView(communitiesView);
        setNavActive(communitiesButton);
        loadCommunities();
    }

    @FXML public void openCreateStyle() {
        activateView(createStyleView);
        setNavActive(createStyleButton);
        filterClothingGrid(searchField.getText());
    }

    @FXML public void openProfile() {
        activateView(profileView);
        setNavActive(profileButton);
        loadOwnProfile();
    }

    @FXML public void openSettings() {
        activateView(settingsView);
        setNavActive(settingsButton);
        syncSettingsUi();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Home – tab switching
    // ═════════════════════════════════════════════════════════════════════════

    @FXML public void openForYou() {
        isForYouActive = true;
        forYouButton.setStyle(TAB_ACTIVE);
        discoverButton.setStyle(TAB_INACTIVE);
        loadForYouFeed();
    }

    @FXML public void openDiscover() {
        isForYouActive = false;
        discoverButton.setStyle(TAB_ACTIVE);
        forYouButton.setStyle(TAB_INACTIVE);
        loadDiscoverFeed();
    }

    @FXML public void openLucky() {
        try {
            List<Post> posts = postService.getAllPosts();
            if (!posts.isEmpty()) openOtherProfile(posts.get((int)(Math.random() * posts.size())).getAuthorId());
        } catch (Exception ex) { logger.warn("Lucky button failed", ex); }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Feed loading
    // ═════════════════════════════════════════════════════════════════════════

    private void loadForYouFeed() {
        feedVBox.getChildren().clear();
        try {
            List<Post> posts = postService.getAllPosts();
            if (posts.isEmpty()) feedVBox.getChildren().add(infoLabel("No posts yet – be the first to share a style!"));
            else for (Post p : posts) feedVBox.getChildren().add(buildPostCard(p));
        } catch (Exception ex) {
            logger.error("Error loading For-You feed", ex);
            feedVBox.getChildren().add(infoLabel("Could not load feed: " + ex.getMessage()));
        }
    }

    private void loadDiscoverFeed() {
        feedVBox.getChildren().clear();
        try {
            List<Post> posts = postService.getAllPosts();
            if (posts.isEmpty()) feedVBox.getChildren().add(infoLabel("Nothing to discover yet."));
            else {
                java.util.Collections.shuffle(posts);
                for (Post p : posts) feedVBox.getChildren().add(buildPostCard(p));
            }
        } catch (Exception ex) {
            logger.error("Error loading Discover feed", ex);
            feedVBox.getChildren().add(infoLabel("Could not load feed: " + ex.getMessage()));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Communities
    // ═════════════════════════════════════════════════════════════════════════

    private void loadCommunities() {
        communityGrid.getChildren().clear();
        try {
            List<Community> communities = communityService.getAllCommunities();
            int col = 0, row = 0;
            for (Community c : communities) {
                Button btn = new Button(c.getName());
                btn.setPrefHeight(75);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-background-color: #cfc6c2; -fx-border-color: #745a42; " +
                        "-fx-border-radius: 15; -fx-padding: 10px; -fx-background-radius: 15px; -fx-font-size: 19px;");
                final int id = c.getCommunityId(); final String name = c.getName();
                btn.setOnAction(e -> openCommunityDetail(id, name));
                communityGrid.add(btn, col, row);
                if (++col == 3) { col = 0; row++; }
            }
        } catch (Exception ex) {
            logger.error("Error loading communities", ex);
            communityGrid.add(infoLabel("Could not load communities: " + ex.getMessage()), 0, 0, 3, 1);
        }
    }

    @FXML private void handleCommunityClick(javafx.event.ActionEvent event) {
        openCommunityDetail(-1, ((Button) event.getSource()).getText());
    }

    @FXML private void handleCreateCommunity() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Community");
        dialog.setHeaderText("Create a new MUSE Community");
        dialog.setContentText("Please enter community name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                communityService.createCommunity(name);
                logger.info("Successfully created community: {}", name);
                loadCommunities();
            } catch (IllegalArgumentException ex) { showErrorAlert("Validation Error", ex.getMessage());
            } catch (Exception ex) {
                logger.error("Error creating community", ex);
                showErrorAlert("Database Error", "Could not save community: " + ex.getMessage());
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Create Style – category buttons + search filtering
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Called by each category button (wired via onAction in FXML).
     *
     * Clicking an already-selected category deselects it (toggles back to "show all"),
     * then immediately re-applies the current search text so both filters always
     * work together.
     */
    @FXML
    private void selectCategory(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();

        ClothingCategory clickedCategory;
        try {
            clickedCategory = ClothingCategory.valueOf(clicked.getText());
        } catch (IllegalArgumentException ex) {
            logger.error("Unknown category button text: {}", clicked.getText());
            return;
        }

        // Toggle: re-clicking the active category clears it
        currentCategory = (clickedCategory == currentCategory) ? null : clickedCategory;

        // Sync highlight state on all category buttons
        for (Button b : new Button[]{ hatButton, topButton, dressButton,
                coatButton, purseButton, bottomButton, shoesButton }) {
            try {
                boolean selected = currentCategory != null
                        && ClothingCategory.valueOf(b.getText()) == currentCategory;
                b.setStyle(selected ? CATEGORY_SELECTED : CATEGORY_UNSELECTED);
            } catch (IllegalArgumentException e) {
                b.setStyle(CATEGORY_UNSELECTED);
            }
        }

        // Re-filter with the current search text + new category
        filterClothingGrid(searchField.getText());
    }

    /**
     * Core filtering method for the Create Style clothing grid.
     *
     * Combines the active category filter and the search query, delegating
     * scoring to {@link SearchService#filterClothing}. Both filters are
     * applied simultaneously:
     * 
     *Category only → shows all items in that category, unranked.
     *Search text only → scores all items against the query, all categories.
     *Both → scores items in the selected category against the query.
     *Neither → shows all items (entry state).
     *
     * @param query current text in the sidebar search field (null or blank = no text filter)
     */
    private void filterClothingGrid(String query) {
        clothingItemsGrid.getChildren().clear();

        ArrayList<ClothingItem> filtered = searchService.filterClothing(query, currentCategory);

        if (filtered.isEmpty()) {
            String message = (query != null && !query.trim().isEmpty())
                    ? "No items match \"" + query.trim() + "\"."
                    : "No items in this category.";
            Label empty = infoLabel(message);
            GridPane.setColumnSpan(empty, 2);
            clothingItemsGrid.getChildren().add(empty);
            return;
        }

        int row = 0, col = 0;
        for (ClothingItem item : filtered) {
            Button itemBtn = new Button();
            itemBtn.setPrefHeight(150);
            itemBtn.setPrefWidth(150);
            itemBtn.setStyle("-fx-background-color: #cfc6c2; -fx-border-color: #745a42; " +
                    "-fx-border-radius: 10; -fx-padding: 5px; -fx-background-radius: 10px; -fx-font-size: 12px;");

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                try {
                    ImageView imgView = new ImageView(new Image(item.getImageUrl()));
                    imgView.setFitHeight(130);
                    imgView.setFitWidth(130);
                    imgView.setPreserveRatio(true);
                    itemBtn.setGraphic(imgView);
                } catch (Exception e) {
                    logger.warn("Could not load image for item: {}", item.getDescription());
                }
            }

            clothingItemsGrid.add(itemBtn, col, row);
            if (++col == 2) { col = 0; row++; }
        }
    }

    @FXML private void handlePostStyle() {
        logger.info("Post Style clicked");
        new Alert(Alert.AlertType.INFORMATION, "Posting outfits coming soon!", ButtonType.OK)
                {{ setHeaderText(null); }}.showAndWait();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Own Profile
    // ═════════════════════════════════════════════════════════════════════════

    private void loadOwnProfile() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current == null) return;
        usernameLabel.setText(current.getUsername());
        myPostsVBox.getChildren().clear();
        myPostsVBox.getChildren().add(sectionHeader("Posts"));
        try {
            List<Post> posts = postService.getPostsByAuthor(SessionManager.getInstance().getCurrentUserId());
            if (posts.isEmpty()) myPostsVBox.getChildren().add(infoLabel("No posts yet."));
            else for (Post p : posts) myPostsVBox.getChildren().add(buildPostCard(p));
        } catch (Exception ex) {
            logger.error("Error loading own posts", ex);
            myPostsVBox.getChildren().add(infoLabel("Could not load posts."));
        }
        savedOutfitsVBox.getChildren().clear();
        savedOutfitsVBox.getChildren().add(sectionHeader("Saved Outfits"));
        savedOutfitsVBox.getChildren().add(infoLabel("No saved outfits yet."));
    }

    @FXML private void handleShowFollowers() {
        profileView.setVisible(false); profileView.setManaged(false);
        followersListView.setVisible(true); followersListView.setManaged(true);
        followersListVBox.getChildren().clear();
        try {
            List<User> followers = userService.getFollowers(SessionManager.getInstance().getCurrentUserId());
            if (followers.isEmpty()) followersListVBox.getChildren().add(infoLabel("You don't have any followers yet."));
            else for (User u : followers) followersListVBox.getChildren().add(createFollowUserLabel(u.getUsername()));
        } catch (Exception ex) {
            logger.error("Error loading followers", ex);
            followersListVBox.getChildren().add(infoLabel("Could not load followers: " + ex.getMessage()));
        }
    }

    @FXML private void handleShowFollowing() {
        profileView.setVisible(false); profileView.setManaged(false);
        followingListView.setVisible(true); followingListView.setManaged(true);
        followingListVBox.getChildren().clear();
        try {
            List<User> following = userService.getFollowing(SessionManager.getInstance().getCurrentUserId());
            if (following.isEmpty()) followingListVBox.getChildren().add(infoLabel("You aren't following anyone yet."));
            else for (User u : following) followingListVBox.getChildren().add(createFollowUserLabel(u.getUsername()));
        } catch (Exception ex) {
            logger.error("Error loading following list", ex);
            followingListVBox.getChildren().add(infoLabel("Could not load following list: " + ex.getMessage()));
        }
    }

    @FXML private void closeFollowLists() {
        followersListView.setVisible(false); followersListView.setManaged(false);
        followingListView.setVisible(false); followingListView.setManaged(false);
        openProfile();
    }

    private Label createFollowUserLabel(String username) {
        Label label = new Label("@" + username);
        label.setMaxWidth(Double.MAX_VALUE);
        String base = "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-padding: 15; " +
                "-fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold;";
        String hover = "-fx-background-color: #779946; -fx-text-fill: white; -fx-padding: 15; " +
                "-fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;";
        label.setStyle(base);
        label.setOnMouseEntered(e -> label.setStyle(hover));
        label.setOnMouseExited(e -> label.setStyle(base));
        return label;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Settings
    // ═════════════════════════════════════════════════════════════════════════

    private void syncSettingsUi() {
        publicButton.setStyle(profileIsPublic
                ? "-fx-background-color: #779946; -fx-border-color: #8c9c76; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 10 20;"
                : "-fx-background-color: transparent; -fx-border-color: #8c9c76; -fx-border-radius: 15; -fx-padding: 10 20;");
        privateButton.setStyle(!profileIsPublic
                ? "-fx-background-color: #779946; -fx-border-color: #8c9c76; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 10 20;"
                : "-fx-background-color: transparent; -fx-border-color: #8c9c76; -fx-border-radius: 15; -fx-padding: 10 20;");
        explicitFilterToggle.setText(explicitFilterOn ? "ON" : "OFF");
        explicitFilterToggle.setStyle(explicitFilterOn
                ? "-fx-background-color: #779946; -fx-border-color: #8c9c76; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 10 20;"
                : "-fx-background-color: #ab2c2c; -fx-text-fill: white; -fx-border-color: #8c9c76; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 10 20;");
    }

    @FXML private void handleSetPublic()  { profileIsPublic = true;  syncSettingsUi(); }
    @FXML private void handleSetPrivate() { profileIsPublic = false; syncSettingsUi(); }
    @FXML private void handleToggleExplicitFilter() { explicitFilterOn = !explicitFilterOn; syncSettingsUi(); }
    @FXML private void handleBlockedProfiles() { logger.info("Blocked Profiles clicked"); }

    @FXML private void handleContactSupport() {
        User current = SessionManager.getInstance().getCurrentUser();
        String username = (current != null) ? current.getUsername() : "unknown";
        try {
            String uri = "mailto:muse.supportt@gmail.com"
                    + "?subject=" + java.net.URLEncoder.encode("MUSE Support Request – @" + username, "UTF-8").replace("+", "%20")
                    + "&body="    + java.net.URLEncoder.encode("Username: @" + username + "\n\nDescribe your issue below:\n", "UTF-8").replace("+", "%20");
            java.awt.Desktop.getDesktop().mail(new java.net.URI(uri));
        } catch (Exception ex) { showSupportFallbackDialog(username); }
    }

    private void showSupportFallbackDialog(String username) {
        new Alert(Alert.AlertType.INFORMATION, "Please email us at:\n\nmuse.supportt@gmail.com\n\nInclude your username (@" + username + ").", ButtonType.OK)
                {{ setTitle("Contact Support"); setHeaderText("We couldn't open your mail client."); }}.showAndWait();
    }

    @FXML private void handleResetProfile() {
        new Alert(Alert.AlertType.CONFIRMATION, "This will reset all your profile data. Are you sure?", ButtonType.YES, ButtonType.NO)
                {{ setHeaderText("Reset Profile"); }}.showAndWait()
                .ifPresent(btn -> { if (btn == ButtonType.YES) logger.info("Profile reset confirmed"); });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Community Detail
    // ═════════════════════════════════════════════════════════════════════════

    public void openCommunityDetail(int communityId, String communityName) {
        currentCommunityId = communityId;
        communityNameLabel.setText(communityName);
        communityPostsVBox.getChildren().clear();
        try {
            List<Post> posts = postService.getPostsByCommunity(communityId);
            if (posts.isEmpty()) communityPostsVBox.getChildren().add(infoLabel("No posts in this community yet."));
            else for (Post p : posts) communityPostsVBox.getChildren().add(buildPostCard(p));
        } catch (Exception ex) {
            logger.error("Error loading community posts", ex);
            communityPostsVBox.getChildren().add(infoLabel("Could not load posts: " + ex.getMessage()));
        }
        activateView(communityDetailView);
        setNavActive(communitiesButton);
    }

    @FXML private void handleAddPostToCommunity() { openCreateStyle(); }

    // ═════════════════════════════════════════════════════════════════════════
    //  Other-User Profile
    // ═════════════════════════════════════════════════════════════════════════

    public void openOtherProfile(int userId) {
        currentOtherUserId = userId;
        updateFollowButton();
        otherPostsVBox.getChildren().clear();
        otherPostsVBox.getChildren().add(sectionHeader("Posts"));
        try {
            var userOpt = userService.getUserById(userId);
            otherUsernameLabel.setText(userOpt.isPresent() ? userOpt.get().getUsername() : "Unknown User");
            List<Post> posts = postService.getPostsByAuthor(userId);
            if (posts.isEmpty()) otherPostsVBox.getChildren().add(infoLabel("No posts yet."));
            else for (Post p : posts) otherPostsVBox.getChildren().add(buildPostCard(p));
        } catch (Exception ex) {
            logger.error("Error loading other user profile", ex);
            otherPostsVBox.getChildren().add(infoLabel("Could not load profile: " + ex.getMessage()));
        }
        int me = SessionManager.getInstance().getCurrentUserId();
        followUserButton.setVisible(userId != me);
        followUserButton.setManaged(userId != me);
        activateView(otherProfileView);
    }

    @FXML private void handleFollowUser() {
        try {
            int me = SessionManager.getInstance().getCurrentUserId();
            if (userService.isFollowing(me, currentOtherUserId)) {
                userService.unfollowUser(me, currentOtherUserId);
                followUserButton.setText("Follow");
            } else {
                userService.followUser(me, currentOtherUserId);
                followUserButton.setText("Following ✓");
            }
        } catch (Exception ex) {
            logger.error("Error handling follow/unfollow", ex);
            showErrorAlert("Error", "Operation failed: " + ex.getMessage());
        }
    }

    private void updateFollowButton() {
        try {
            followUserButton.setText(userService.isFollowing(
                    SessionManager.getInstance().getCurrentUserId(), currentOtherUserId)
                    ? "Following ✓" : "Follow");
        } catch (Exception ex) { followUserButton.setText("Follow"); }
    }

    /**
     * Triggered when the user confirms a search (Enter / search button).
     *
     * In Create Style the real-time listener already handles filtering on every
     * keystroke, so pressing Enter there is a no-op. In all other views this
     * method runs a cross-entity search and shows results in the home feed.
     */
    @FXML
    private void handleSearch() {
        if (createStyleView.isVisible()) return; // handled by the real-time listener

        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        logger.info("Global search query: {}", query);

        SearchResult people     = searchService.searchAll(query, SearchType.PEOPLE);
        SearchResult communities = searchService.searchAll(query, SearchType.COMMUNITIES);
        SearchResult posts      = searchService.searchAll(query, SearchType.POSTS);

        feedVBox.getChildren().clear();
        feedVBox.getChildren().add(infoLabel("Search results for: \"" + query + "\""));
        boolean anyResults = false;

        if (!people.getUsers().isEmpty()) {
            anyResults = true;
            feedVBox.getChildren().add(sectionHeader("People"));
            for (User u : people.getUsers()) {
                String display = u.getUsername();
                Label lbl = new Label(display);
                lbl.setStyle("-fx-font-size: 14px; -fx-padding: 8; " +
                        "-fx-background-color: #C0B7AD; -fx-background-radius: 10; -fx-cursor: hand;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setOnMouseClicked(e -> openOtherProfile(u.getUserId()));
                feedVBox.getChildren().add(lbl);
            }
        }

        if (!communities.getCommunities().isEmpty()) {
            anyResults = true;
            feedVBox.getChildren().add(sectionHeader("Communities"));
            for (Community c : communities.getCommunities()) {
                Button btn = new Button(c.getName());
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setPrefHeight(60);
                btn.setStyle("-fx-background-color: #cfc6c2; -fx-border-color: #745a42; " +
                        "-fx-border-radius: 15; -fx-background-radius: 15; -fx-font-size: 16px;");
                btn.setOnAction(e -> openCommunityDetail(c.getCommunityId(), c.getName()));
                feedVBox.getChildren().add(btn);
            }
        }

        if (!posts.getPosts().isEmpty()) {
            anyResults = true;
            feedVBox.getChildren().add(sectionHeader("Posts"));
            for (Post p : posts.getPosts()) feedVBox.getChildren().add(buildPostCard(p));
        }

        if (!anyResults) feedVBox.getChildren().add(infoLabel("No results found for \"" + query + "\"."));

        activateView(homeView);
        setNavActive(homeButton);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Logout
    // ═════════════════════════════════════════════════════════════════════════

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception ex) { logger.error("Error navigating to login", ex); }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Private helpers
    // ═════════════════════════════════════════════════════════════════════════

    private void activateView(VBox target) {
        for (VBox view : new VBox[]{ homeView, communitiesView, createStyleView,
                profileView, settingsView, communityDetailView, otherProfileView }) {
            view.setVisible(view == target);
            view.setManaged(view == target);
        }
    }

    private void setNavActive(Button active) {
        for (Button btn : new Button[]{ homeButton, communitiesButton,
                createStyleButton, profileButton, settingsButton })
            btn.setStyle(btn == active ? NAV_ACTIVE : NAV_INACTIVE);
    }

    private Node buildPostCard(Post post) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #C0B7AD; -fx-background-radius: 15; -fx-padding: 12;");
        card.setMaxWidth(Double.MAX_VALUE);
        Label author = new Label("by @" + post.getAuthorUsername());
        author.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a4a4a; -fx-cursor: hand;");
        author.setOnMouseClicked(e -> openOtherProfile(post.getAuthorId()));
        card.getChildren().add(author);
        return card;
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }

    private Label infoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 10;");
        label.setWrapText(true);
        return label;
    }

    private Label sectionHeader(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setPrefHeight(44);
        label.setStyle("-fx-background-color: #745a42; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 28px;");
        return label;
    }
}