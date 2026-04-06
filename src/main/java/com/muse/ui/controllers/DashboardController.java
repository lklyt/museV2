package com.muse.ui.controllers;

import com.muse.models.Community;
import com.muse.models.ClothingItem;
import com.muse.models.Post;
import com.muse.models.User;
import com.muse.models.ClothingCategory;
import com.muse.service.CommunityService;
import com.muse.service.PostService;
import com.muse.service.UserService;
import com.muse.service.ClothingItemService;
import com.muse.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unified controller for {@code dashboard.fxml}.
 *
 * <p>
 * All in-app navigation is handled here by toggling {@code visible} /
 * {@code managed}
 * on the named view VBoxes that live inside the centre {@code StackPane}. No
 * additional
 * controller classes are needed for the inner views (Communities, CreateStyle,
 * Profile,
 * Settings, CommunityDetail, OtherProfile).
 *
 * <p>
 * <b>FXML views managed by this controller</b>
 * <ul>
 * <li>{@code homeView} – For-You / Discover feed</li>
 * <li>{@code communitiesView} – Community grid + Create Community</li>
 * <li>{@code createStyleView} – Outfit builder</li>
 * <li>{@code profileView} – Current user's profile</li>
 * <li>{@code settingsView} – App settings</li>
 * <li>{@code communityDetailView} – Single community (posts feed)</li>
 * <li>{@code otherProfileView} – Another user's profile</li>
 * </ul>
 */
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    // ── Services ─────────────────────────────────────────────────────────────
    private final PostService postService = new PostService();
    private final CommunityService communityService = new CommunityService();
    private final UserService userService = new UserService();
    private final ClothingItemService clothingItemService = new ClothingItemService();

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML
    private TextField searchField;
    @FXML
    private Button homeButton;
    @FXML
    private Button communitiesButton;
    @FXML
    private Button createStyleButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    // ── View containers (StackPane children) ──────────────────────────────────
    @FXML
    private VBox homeView;
    @FXML
    private VBox communitiesView;
    @FXML
    private VBox createStyleView;
    @FXML
    private VBox profileView;
    @FXML
    private VBox settingsView;
    @FXML
    private VBox communityDetailView;
    @FXML
    private VBox otherProfileView;

    // ── Home view ─────────────────────────────────────────────────────────────
    @FXML
    private Button forYouButton;
    @FXML
    private Button discoverButton;
    @FXML
    private Button luckyButton;
    @FXML
    private VBox feedVBox;

    // ── Communities view ──────────────────────────────────────────────────────
    @FXML
    private GridPane communityGrid;
    @FXML
    private Button createCommunityButton;

    // ── Create-Style view ─────────────────────────────────────────────────────
    @FXML
    private AnchorPane outfitPreviewPane;
    @FXML
    private Button postStyleButton;
    @FXML
    private GridPane clothingItemsGrid;
    // Category buttons
    @FXML
    private Button hatButton;
    @FXML
    private Button topButton;
    @FXML
    private Button dressButton;
    @FXML
    private Button coatButton;
    @FXML
    private Button purseButton;
    @FXML
    private Button bottomButton;
    @FXML
    private Button shoesButton;

    // ── Own-profile view ──────────────────────────────────────────────────────
    @FXML
    private Label usernameLabel;
    @FXML
    private Button followersButton;
    @FXML
    private Button followingButton;
    @FXML
    private VBox savedOutfitsVBox;
    @FXML
    private VBox myPostsVBox;
    @FXML
    private VBox followersListView;
    @FXML
    private VBox followersListVBox;
    @FXML
    private VBox followingListView;
    @FXML
    private VBox followingListVBox;

    // ── Settings view ─────────────────────────────────────────────────────────
    @FXML
    private Button publicButton;
    @FXML
    private Button privateButton;
    @FXML
    private Button blockedProfilesButton;
    @FXML
    private Button explicitFilterToggle;
    @FXML
    private Button contactSupportButton;
    @FXML
    private Button resetProfileButton;

    // ── Community-detail view ────────────────────────────────────────────────
    @FXML
    private Label communityNameLabel;
    @FXML
    private VBox communityPostsVBox;
    @FXML
    private Button addPostToCommunityButton;

    // ── Other-profile view ───────────────────────────────────────────────────
    @FXML
    private Label otherUsernameLabel;
    @FXML
    private Button followUserButton;
    @FXML
    private VBox otherPostsVBox;

    // ── Style constants ───────────────────────────────────────────────────────
    private static final String NAV_ACTIVE = "-fx-background-color: #8c9c76; -fx-text-fill: white; " +
            "-fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 16px;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 16px;";
    private static final String TAB_ACTIVE = "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-background-radius: 15;";
    private static final String TAB_INACTIVE = "-fx-background-color: transparent; -fx-border-color: #8c9c76; -fx-border-radius: 15;";
    private static final String CATEGORY_ACTIVE = "-fx-background-color: #745a42; -fx-text-fill: white; -fx-background-radius: 15;";
    private static final String CATEGORY_INACTIVE = "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-background-radius: 15;";
    private static final String ITEM_BUTTON_BASE = "-fx-background-color: #cfc6c2; -fx-border-color: #745a42; " +
            "-fx-border-radius: 10; -fx-padding: 5px; -fx-background-radius: 10px; -fx-font-size: 12px;";
    private static final String ITEM_BUTTON_SELECTED = "-fx-background-color: #d9cfc6; -fx-border-color: #745a42; " +
            "-fx-border-width: 3; -fx-border-radius: 10; -fx-padding: 5px; -fx-background-radius: 10px; -fx-font-size: 12px;";

    private static final ClothingCategory[] PREVIEW_RENDER_ORDER = {
            ClothingCategory.BOTTOMS,
            ClothingCategory.DRESSES,
            ClothingCategory.SHOES,
            ClothingCategory.TOPS,
            ClothingCategory.COATS,
            ClothingCategory.HATS,
            ClothingCategory.PURSES
    };

    /** Tracks which home-tab (forYou / discover) is currently active. */
    private boolean isForYouActive = true;
    /** Id of the community currently shown in communityDetailView. */
    private int currentCommunityId = -1;
    /** Id of the user currently shown in otherProfileView. */
    private int currentOtherUserId = -1;
    /** Whether explicit-comment filter is on. */
    private boolean explicitFilterOn = true;
    /** Whether the logged-in user's profile is public. */
    private boolean profileIsPublic = true;
    /** Currently selected category in the Create Style view. */
    private ClothingCategory selectedCreateStyleCategory = ClothingCategory.HATS;
    /** Outfit composition currently selected by the user. */
    private final Map<ClothingCategory, ClothingItem> selectedOutfitItems = new EnumMap<>(ClothingCategory.class);

    // ═════════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        // Sidebar buttons are wired via onAction in FXML; the explicit setOnAction
        // calls below provide a safe fallback for any programmatic instantiation.
        homeButton.setOnAction(e -> openHome());
        communitiesButton.setOnAction(e -> openCommunities());
        createStyleButton.setOnAction(e -> openCreateStyle());
        profileButton.setOnAction(e -> openProfile());
        settingsButton.setOnAction(e -> openSettings());
        logoutButton.setOnAction(e -> handleLogout());

        // Tab bar
        forYouButton.setOnAction(e -> openForYou());
        discoverButton.setOnAction(e -> openDiscover());
        luckyButton.setOnAction(e -> openLucky());

        initializeCreateStyleComposer();

        // Load the default view
        openHome();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Navigation – sidebar
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void openHome() {
        activateView(homeView);
        setNavActive(homeButton);
        // Re-draw the correct tab
        if (isForYouActive)
            openForYou();
        else
            openDiscover();
    }

    @FXML
    public void openCommunities() {
        activateView(communitiesView);
        setNavActive(communitiesButton);
        loadCommunities();
    }

    @FXML
    public void openCreateStyle() {
        activateView(createStyleView);
        setNavActive(createStyleButton);
        highlightCategorySelection(buttonForCategory(selectedCreateStyleCategory));
        renderCurrentOutfitPreview();

        try {
            loadClothingItems(selectedCreateStyleCategory);
        } catch (Exception ex) {
            logger.error("Error refreshing create-style items", ex);
            clothingItemsGrid.getChildren().clear();
            Label errorLabel = infoLabel("Could not load items.");
            GridPane.setColumnSpan(errorLabel, 2);
            clothingItemsGrid.getChildren().add(errorLabel);
        }
    }

    @FXML
    public void openProfile() {
        activateView(profileView);
        setNavActive(profileButton);
        loadOwnProfile();
    }

    @FXML
    public void openSettings() {
        activateView(settingsView);
        setNavActive(settingsButton);
        syncSettingsUi();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Home – tab switching
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void openForYou() {
        isForYouActive = true;
        forYouButton.setStyle(TAB_ACTIVE);
        discoverButton.setStyle(TAB_INACTIVE);
        loadForYouFeed();
    }

    @FXML
    public void openDiscover() {
        isForYouActive = false;
        discoverButton.setStyle(TAB_ACTIVE);
        forYouButton.setStyle(TAB_INACTIVE);
        loadDiscoverFeed();
    }

    @FXML
    public void openLucky() {
        // Pick a random post / community and navigate to it
        try {
            List<Post> posts = postService.getAllPosts();
            if (!posts.isEmpty()) {
                int idx = (int) (Math.random() * posts.size());
                Post p = posts.get(idx);
                openOtherProfile(p.getAuthorId());
            }
        } catch (Exception ex) {
            logger.warn("Lucky button failed", ex);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Feed loading
    // ═════════════════════════════════════════════════════════════════════════

    private void loadForYouFeed() {
        feedVBox.getChildren().clear();
        try {
            List<Post> posts = postService.getAllPosts();
            if (posts.isEmpty()) {
                feedVBox.getChildren().add(infoLabel("No posts yet – be the first to share a style!"));
            } else {
                for (Post post : posts) {
                    feedVBox.getChildren().add(buildPostCard(post));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading For-You feed", ex);
            feedVBox.getChildren().add(infoLabel("Could not load feed: " + ex.getMessage()));
        }
    }

    private void loadDiscoverFeed() {
        feedVBox.getChildren().clear();
        try {
            // Discover shows all posts sorted differently; reuse getAllPosts for now.
            // Replace with postService.getDiscoverPosts() when that method exists.
            List<Post> posts = postService.getAllPosts();
            if (posts.isEmpty()) {
                feedVBox.getChildren().add(infoLabel("Nothing to discover yet."));
            } else {
                java.util.Collections.shuffle(posts); // simple "discover" shuffle
                for (Post post : posts) {
                    feedVBox.getChildren().add(buildPostCard(post));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading Discover feed", ex);
            feedVBox.getChildren().add(infoLabel("Could not load feed: " + ex.getMessage()));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Communities
    // ═════════════════════════════════════════════════════════════════════════

    private void loadCommunities() {
        communityGrid.getChildren().clear();

        try {
            List<Community> communities = communityService.getAllCommunities();

            int col = 0;
            int row = 0;

            for (Community c : communities) {
                Button btn = new Button(c.getName());
                btn.setPrefHeight(75);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-background-color: #cfc6c2; -fx-border-color: #745a42; " +
                        "-fx-border-radius: 15; -fx-padding: 10px; -fx-background-radius: 15px; " +
                        "-fx-font-size: 19px;");

                final int communityId = c.getCommunityId();
                final String communityName = c.getName();
                btn.setOnAction(e -> openCommunityDetail(communityId, communityName));

                communityGrid.add(btn, col, row);

                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            }

        } catch (Exception ex) {
            logger.error("Error loading communities", ex);
            communityGrid.add(infoLabel("Could not load communities: " + ex.getMessage()), 0, 0, 3, 1);
        }
    }

    @FXML
    private void handleCommunityClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String communityName = clickedButton.getText();

        // 2. Pass a dummy ID (-1) and the name to your official method!
        openCommunityDetail(-1, communityName);
    }

    @FXML
    private void handleCreateCommunity() {
         // 1. Setup the Input Dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Community");
        dialog.setHeaderText("Create a new MUSE Community");
        dialog.setContentText("Please enter community name:");

        // 2. Capture the result
        Optional<String> result = dialog.showAndWait();

        // 3. If the user clicked OK and provided a name
        result.ifPresent(name -> {
            try {
                // Call the service to save to DB
                communityService.createCommunity(name);
                
                logger.info("Successfully created community: {}", name);

                // 4. REFRESH the grid so the new community appears
                loadCommunities();

            } catch (IllegalArgumentException ex) {
                // This catches "Name already exists" or "Too short" from your Service
                showErrorAlert("Validation Error", ex.getMessage());
            } catch (Exception ex) {
                logger.error("Error creating community", ex);
                showErrorAlert("Database Error", "Could not save community: " + ex.getMessage());
            }
        });
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Create Style
    // ═════════════════════════════════════════════════════════════════════════

    private void initializeCreateStyleComposer() {
        highlightCategorySelection(buttonForCategory(selectedCreateStyleCategory));
        renderCurrentOutfitPreview();

        try {
            loadClothingItems(selectedCreateStyleCategory);
        } catch (Exception ex) {
            logger.error("Could not initialize create-style items", ex);
            clothingItemsGrid.getChildren().clear();
            Label errorLabel = infoLabel("Could not load style items.");
            GridPane.setColumnSpan(errorLabel, 2);
            clothingItemsGrid.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void selectCategory(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();

        try {
            String categoryName = clicked.getText();
            ClothingCategory category = ClothingCategory.valueOf(categoryName);

            selectedCreateStyleCategory = category;
            highlightCategorySelection(clicked);
            loadClothingItems(category);
            logger.info("Category selected: {}", categoryName);
        } catch (Exception ex) {
            logger.error("Error loading clothing items for selected category", ex);
            clothingItemsGrid.getChildren().clear();
            Label errorLabel = infoLabel("Could not load items.");
            GridPane.setColumnSpan(errorLabel, 2);
            clothingItemsGrid.getChildren().add(errorLabel);
        }
    }

    private void highlightCategorySelection(Button selected) {
        for (Button button : new Button[] { hatButton, topButton, dressButton, coatButton, purseButton, bottomButton, shoesButton }) {
            button.setStyle(button == selected ? CATEGORY_ACTIVE : CATEGORY_INACTIVE);
        }
    }

    private Button buttonForCategory(ClothingCategory category) {
        return switch (category) {
            case HATS -> hatButton;
            case TOPS -> topButton;
            case DRESSES -> dressButton;
            case COATS -> coatButton;
            case PURSES -> purseButton;
            case BOTTOMS -> bottomButton;
            case SHOES -> shoesButton;
        };
    }

    private void loadClothingItems(ClothingCategory category) throws Exception {
        clothingItemsGrid.getChildren().clear();

        var items = clothingItemService.getItemsWithCachedImages(category);
        if (items.isEmpty()) {
            Label noItemsLabel = infoLabel("No items in this category.");
            GridPane.setColumnSpan(noItemsLabel, 2);
            clothingItemsGrid.getChildren().add(noItemsLabel);
            return;
        }

        int row = 0;
        int col = 0;
        for (var item : items) {
            Button itemBtn = new Button();
            itemBtn.setPrefHeight(150);
            itemBtn.setPrefWidth(150);
            itemBtn.setWrapText(true);
            itemBtn.setStyle(isCurrentCategorySelection(category, item)
                    ? ITEM_BUTTON_SELECTED
                    : ITEM_BUTTON_BASE);
            itemBtn.setOnAction(e -> handleOutfitItemSelection(category, item));

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                try {
                    Image img = new Image(item.getImageUrl());
                    ImageView imgView = new ImageView(img);
                    imgView.setFitHeight(130);
                    imgView.setFitWidth(130);
                    imgView.setPreserveRatio(true);
                    itemBtn.setGraphic(imgView);
                } catch (Exception e) {
                    logger.warn("Could not load image for item: {}", item.getDescription());
                    itemBtn.setText(item.getDescription());
                }
            } else {
                itemBtn.setText(item.getDescription());
            }

            clothingItemsGrid.add(itemBtn, col, row);
            col++;
            if (col == 2) {
                col = 0;
                row++;
            }
        }
    }

    private boolean isCurrentCategorySelection(ClothingCategory category, ClothingItem candidate) {
        ClothingItem selected = selectedOutfitItems.get(category);
        if (selected == null) {
            return false;
        }
        return isSameClothingItem(selected, candidate);
    }

    private boolean isSameClothingItem(ClothingItem left, ClothingItem right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.getId() > 0 && right.getId() > 0) {
            return left.getId() == right.getId();
        }
        return java.util.Objects.equals(left.getCategory(), right.getCategory())
                && java.util.Objects.equals(left.getDescription(), right.getDescription())
                && java.util.Objects.equals(left.getImageUrl(), right.getImageUrl());
    }

    private void handleOutfitItemSelection(ClothingCategory category, ClothingItem item) {
        selectedOutfitItems.put(category, item);
        enforceOutfitCombinationRules(category);
        renderCurrentOutfitPreview();

        try {
            loadClothingItems(selectedCreateStyleCategory);
        } catch (Exception ex) {
            logger.error("Could not refresh selected clothing state", ex);
        }
    }

    private void enforceOutfitCombinationRules(ClothingCategory changedCategory) {
        if (changedCategory == ClothingCategory.DRESSES) {
            selectedOutfitItems.remove(ClothingCategory.TOPS);
            selectedOutfitItems.remove(ClothingCategory.BOTTOMS);
        }

        if (changedCategory == ClothingCategory.TOPS || changedCategory == ClothingCategory.BOTTOMS) {
            selectedOutfitItems.remove(ClothingCategory.DRESSES);
        }
    }

    private void renderCurrentOutfitPreview() {
        renderOutfitPreview(outfitPreviewPane, selectedOutfitItems, true);
    }

    private void renderOutfitPreview(AnchorPane targetPane, Map<ClothingCategory, ClothingItem> selectedItems,
            boolean showPlaceholderIfEmpty) {
        if (targetPane == null) {
            return;
        }

        targetPane.getChildren().clear();

        double paneWidth = targetPane.getWidth() > 0 ? targetPane.getWidth() : targetPane.getPrefWidth();
        double paneHeight = targetPane.getHeight() > 0 ? targetPane.getHeight() : targetPane.getPrefHeight();

        boolean hasRenderableItem = false;
        for (ClothingCategory category : PREVIEW_RENDER_ORDER) {
            ClothingItem item = selectedItems.get(category);
            if (item == null || item.getImageUrl() == null || item.getImageUrl().isBlank()) {
                continue;
            }

            ImageView imageView = createPreviewImageView(item, category, paneWidth, paneHeight);
            if (imageView != null) {
                targetPane.getChildren().add(imageView);
                hasRenderableItem = true;
            }
        }

        if (!hasRenderableItem && showPlaceholderIfEmpty) {
            Label placeholder = infoLabel("Select items to preview");
            placeholder.setStyle("-fx-text-fill: #8a847e; -fx-font-size: 14px;");
            placeholder.setLayoutX(Math.max(16, paneWidth * 0.19));
            placeholder.setLayoutY(Math.max(20, paneHeight * 0.45));
            targetPane.getChildren().add(placeholder);
        }
    }

    private ImageView createPreviewImageView(ClothingItem item, ClothingCategory category, double paneWidth, double paneHeight) {
        PreviewSlot slot = previewSlotFor(category);
        if (slot == null) {
            return null;
        }

        try {
            Image image = new Image(item.getImageUrl());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(paneWidth * slot.widthRatio());
            imageView.setFitHeight(paneHeight * slot.heightRatio());
            imageView.setLayoutX(paneWidth * slot.xRatio());
            imageView.setLayoutY(paneHeight * slot.yRatio());
            return imageView;
        } catch (Exception ex) {
            logger.warn("Could not render outfit preview image for {}", item.getDescription(), ex);
            return null;
        }
    }

    private PreviewSlot previewSlotFor(ClothingCategory category) {
        return switch (category) {
            case HATS -> new PreviewSlot(0.38, 0.04, 0.24, 0.14);
            case TOPS -> new PreviewSlot(0.25, 0.19, 0.54, 0.35);
            case DRESSES -> new PreviewSlot(0.25, 0.20, 0.55, 0.48);
            case COATS -> new PreviewSlot(0.69, 0.26, 0.27, 0.42);
            case BOTTOMS -> new PreviewSlot(0.28, 0.52, 0.50, 0.28);
            case SHOES -> new PreviewSlot(0.34, 0.80, 0.38, 0.17);
            case PURSES -> new PreviewSlot(0.08, 0.43, 0.34, 0.21);
            default -> null;
        };
    }

    private record PreviewSlot(double xRatio, double yRatio, double widthRatio, double heightRatio) {
    }

    @FXML
    private void handlePostStyle() {
        int authorId = SessionManager.getInstance().getCurrentUserId();
        if (authorId <= 0) {
            showErrorAlert("Not Logged In", "Please log in again before posting a style.");
            return;
        }

        try {
            List<ClothingItem> outfitToPost = buildValidatedOutfitSelection();
            List<Community> communities = communityService.getAllCommunities();
            if (communities.isEmpty()) {
                showErrorAlert("No Communities", "Create a community first before posting a style.");
                return;
            }

            Optional<Community> selectedCommunityOpt = promptCommunitySelection(communities);
            if (selectedCommunityOpt.isEmpty()) {
                return;
            }

            Community selectedCommunity = selectedCommunityOpt.get();
            postService.createPost(authorId, selectedCommunity.getCommunityId(), outfitToPost);
            logger.info("Created style post for user {} in community {}", authorId, selectedCommunity.getCommunityId());

            selectedOutfitItems.clear();
            renderCurrentOutfitPreview();
            loadClothingItems(selectedCreateStyleCategory);

            Alert success = new Alert(Alert.AlertType.INFORMATION,
                    "Your outfit has been posted to " + selectedCommunity.getName() + ".", ButtonType.OK);
            success.setHeaderText("Post created");
            success.showAndWait();

            openCommunityDetail(selectedCommunity.getCommunityId(), selectedCommunity.getName());
        } catch (IllegalStateException ex) {
            showErrorAlert("Incomplete Outfit", ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error posting style", ex);
            showErrorAlert("Post Failed", "Could not post outfit: " + ex.getMessage());
        }
    }

    private List<ClothingItem> buildValidatedOutfitSelection() {
        ClothingItem hat = selectedOutfitItems.get(ClothingCategory.HATS);
        ClothingItem top = selectedOutfitItems.get(ClothingCategory.TOPS);
        ClothingItem bottom = selectedOutfitItems.get(ClothingCategory.BOTTOMS);
        ClothingItem dress = selectedOutfitItems.get(ClothingCategory.DRESSES);
        ClothingItem coat = selectedOutfitItems.get(ClothingCategory.COATS);
        ClothingItem shoes = selectedOutfitItems.get(ClothingCategory.SHOES);
        ClothingItem purse = selectedOutfitItems.get(ClothingCategory.PURSES);

        boolean hasDress = dress != null;
        boolean hasTopAndBottom = top != null && bottom != null;

        if (hasDress && (top != null || bottom != null)) {
            throw new IllegalStateException("Select either a dress, or a top and bottom combination.");
        }

        if (!hasDress && !hasTopAndBottom) {
            throw new IllegalStateException("Please select either a dress, or both a top and bottom.");
        }

        if (shoes == null || purse == null) {
            throw new IllegalStateException("Please select both shoes and a purse.");
        }

        List<ClothingItem> outfitToPost = new ArrayList<>();
        if (hat != null) {
            outfitToPost.add(hat);
        }
        if (hasDress) {
            outfitToPost.add(dress);
        } else {
            outfitToPost.add(top);
            outfitToPost.add(bottom);
        }
        if (coat != null) {
            outfitToPost.add(coat);
        }
        outfitToPost.add(shoes);
        outfitToPost.add(purse);
        return outfitToPost;
    }

    private Optional<Community> promptCommunitySelection(List<Community> communities) {
        String defaultCommunityName = findCommunityNameById(communities, currentCommunityId)
                .orElse(communities.get(0).getName());

        List<String> communityNames = communities.stream()
                .map(Community::getName)
                .toList();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultCommunityName, communityNames);
        dialog.setTitle("Post Outfit");
        dialog.setHeaderText("Choose a community for this style");
        dialog.setContentText("Community:");

        Optional<String> chosenName = dialog.showAndWait();
        if (chosenName.isEmpty()) {
            return Optional.empty();
        }

        return communities.stream()
                .filter(c -> c.getName().equals(chosenName.get()))
                .findFirst();
    }

    private Optional<String> findCommunityNameById(List<Community> communities, int communityId) {
        return communities.stream()
                .filter(c -> c.getCommunityId() == communityId)
                .map(Community::getName)
                .findFirst();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Own Profile
    // ═════════════════════════════════════════════════════════════════════════

    private void loadOwnProfile() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current == null)
            return;

        usernameLabel.setText(current.getUsername());

        myPostsVBox.getChildren().clear();
        // Re-add the header label (it was declared in FXML as a static child)
        myPostsVBox.getChildren().add(sectionHeader("Posts"));

        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            List<Post> posts = postService.getPostsByAuthor(userId);
            if (posts.isEmpty()) {
                myPostsVBox.getChildren().add(infoLabel("No posts yet."));
            } else {
                for (Post p : posts) {
                    myPostsVBox.getChildren().add(buildPostCard(p));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading own posts", ex);
            myPostsVBox.getChildren().add(infoLabel("Could not load posts."));
        }

        // TODO: load saved outfits into savedOutfitsVBox via an OutfitService
        savedOutfitsVBox.getChildren().clear();
        savedOutfitsVBox.getChildren().add(sectionHeader("Saved Outfits"));
        savedOutfitsVBox.getChildren().add(infoLabel("No saved outfits yet."));
    }

    @FXML
    private void handleShowFollowers() {
        logger.info("Show Followers clicked");
        // Hide profile view and show followers view
        profileView.setVisible(false);
        profileView.setManaged(false);
        followersListView.setVisible(true);
        followersListView.setManaged(true);

        followersListVBox.getChildren().clear();

        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            List<User> followers = userService.getFollowers(userId);

            if (followers.isEmpty()) {
                followersListVBox.getChildren().add(infoLabel("You don't have any followers yet."));
            } else {
                for (User user : followers) {
                    followersListVBox.getChildren().add(createFollowUserLabel(user.getUsername()));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading followers", ex);
            followersListVBox.getChildren().add(infoLabel("Could not load followers: " + ex.getMessage()));
        }
    }

    @FXML
    private void handleShowFollowing() {
        logger.info("Show Following clicked");
        // Hide profile view and show following view
        profileView.setVisible(false);
        profileView.setManaged(false);
        followingListView.setVisible(true);
        followingListView.setManaged(true);

        followingListVBox.getChildren().clear();

        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            List<User> following = userService.getFollowing(userId);

            if (following.isEmpty()) {
                followingListVBox.getChildren().add(infoLabel("You aren't following anyone yet."));
            } else {
                for (User user : following) {
                    followingListVBox.getChildren().add(createFollowUserLabel(user.getUsername()));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading following list", ex);
            followingListVBox.getChildren().add(infoLabel("Could not load following list: " + ex.getMessage()));
        }
    }

    /** Helper to safely return to the profile view from the lists */
    @FXML
    private void closeFollowLists() {
        followersListView.setVisible(false);
        followersListView.setManaged(false);
        followingListView.setVisible(false);
        followingListView.setManaged(false);
        openProfile();
    }

    /** Helper to generate the green-themed username rows for the lists */
    private Label createFollowUserLabel(String username) {
        Label label = new Label("@" + username);
        label.setMaxWidth(Double.MAX_VALUE);
        // Base green theme matching your app
        label.setStyle(
                "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Optional hover effect for a bit of polish
        label.setOnMouseEntered(e -> label.setStyle(
                "-fx-background-color: #779946; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;"));
        label.setOnMouseExited(e -> label.setStyle(
                "-fx-background-color: #8c9c76; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 10; -fx-font-size: 16px; -fx-font-weight: bold;"));

        return label;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Settings
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

    @FXML
    private void handleSetPublic() {
        profileIsPublic = true;
        syncSettingsUi();
        /* TODO: persist */ }

    @FXML
    private void handleSetPrivate() {
        profileIsPublic = false;
        syncSettingsUi();
        /* TODO: persist */ }

    @FXML
    private void handleToggleExplicitFilter() {
        explicitFilterOn = !explicitFilterOn;
        syncSettingsUi();
        // TODO: persist preference via a UserPreferenceService
    }

    @FXML
    private void handleBlockedProfiles() {
        // TODO: open a blocked-profiles dialog or sub-view
        logger.info("Blocked Profiles clicked");
    }

    @FXML
    private void handleContactSupport() {
        logger.info("Contact Support clicked");

        User current = SessionManager.getInstance().getCurrentUser();
        String username = (current != null) ? current.getUsername() : "unknown";

        String subject = "MUSE Support Request – @" + username;
        String body = "Username: @" + username + "\n\nDescribe your issue below:\n";

        try {
            String mailtoUri = "mailto:muse.supportt@gmail.com"
                    + "?subject=" + java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20")
                    + "&body="    + java.net.URLEncoder.encode(body,    "UTF-8").replace("+", "%20");

            java.awt.Desktop.getDesktop().mail(new java.net.URI(mailtoUri));

        } catch (UnsupportedOperationException ex) {
            logger.warn("Desktop mail not supported on this OS", ex);
            showSupportFallbackDialog(username);
        } catch (Exception ex) {
            logger.error("Could not open mail client", ex);
            showSupportFallbackDialog(username);
        }
    }

    private void showSupportFallbackDialog(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("Contact Support");
        alert.setHeaderText("We couldn't open your mail client.");
        alert.setContentText(
            "Please email us directly at:\n\n"
            + "muse.supportt@gmail.com\n\n"
            + "Include your username (@" + username + ") in your message."
        );
        alert.showAndWait();
    }
    @FXML
    private void handleResetProfile() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will reset all your profile data. Are you sure?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Reset Profile");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // TODO: call userService.resetProfile(...)
                logger.info("Profile reset confirmed");
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Community Detail
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Navigates to the detail view for a specific community.
     * Called programmatically from community-grid button handlers.
     *
     * @param communityId   database ID of the community
     * @param communityName display name shown in the header
     */
    public void openCommunityDetail(int communityId, String communityName) {
        currentCommunityId = communityId;
        communityNameLabel.setText(communityName);
        communityPostsVBox.getChildren().clear();

        try {
            List<Post> posts = postService.getPostsByCommunity(communityId);
            if (posts.isEmpty()) {
                communityPostsVBox.getChildren().add(infoLabel("No posts in this community yet."));
            } else {
                for (Post p : posts) {
                    communityPostsVBox.getChildren().add(buildPostCard(p));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading community posts", ex);
            communityPostsVBox.getChildren().add(infoLabel("Could not load posts: " + ex.getMessage()));
        }

        activateView(communityDetailView);
        setNavActive(communitiesButton);
    }

    @FXML
    private void handleAddPostToCommunity() {
        logger.info("Redirecting to Create Style from community {}", currentCommunityId);

        // This calls the exact same method that your sidebar uses,
        // which handles hiding the current view, showing the Create Style view,
        // and updating the green highlight on the sidebar button.
        openCreateStyle();
    }
    // ═════════════════════════════════════════════════════════════════════════
    // Other-User Profile
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Navigates to the profile view for another user.
     * Can be called from post-card "username" buttons in the feed.
     *
     * @param userId database ID of the user to display
     */
    public void openOtherProfile(int userId) {
       
        currentOtherUserId = userId;
        updateFollowButton();
        otherPostsVBox.getChildren().clear();
        otherPostsVBox.getChildren().add(sectionHeader("Posts"));

        try {
            var userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                otherUsernameLabel.setText(userOpt.get().getUsername());
            } else {
                otherUsernameLabel.setText("Unknown User");
            }

            List<Post> posts = postService.getPostsByAuthor(userId);
            if (posts.isEmpty()) {
                otherPostsVBox.getChildren().add(infoLabel("No posts yet."));
            } else {
                for (Post p : posts) {
                    otherPostsVBox.getChildren().add(buildPostCard(p));
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading other user profile", ex);
            otherPostsVBox.getChildren().add(infoLabel("Could not load profile: " + ex.getMessage()));
        }

        // Prevent following yourself
        int me = SessionManager.getInstance().getCurrentUserId();
        followUserButton.setVisible(userId != me);
        followUserButton.setManaged(userId != me);

        activateView(otherProfileView);
    }


    @FXML
    private void handleFollowUser() {
        try {
            int me = SessionManager.getInstance().getCurrentUserId();

            if (userService.isFollowing(me, currentOtherUserId)) {
                // UNFOLLOW
                userService.unfollowUser(me, currentOtherUserId);
                followUserButton.setText("Follow");
                logger.info("Unfollowed user {}", currentOtherUserId);

            } else {
                // FOLLOW
                userService.followUser(me, currentOtherUserId);
                followUserButton.setText("Following ✓");
                logger.info("Followed user {}", currentOtherUserId);
            }

        } catch (Exception ex) {
            logger.error("Error handling follow/unfollow", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Operation failed: " + ex.getMessage(),
                    ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void updateFollowButton() {
        int me = SessionManager.getInstance().getCurrentUserId();

        try {
            if (userService.isFollowing(me, currentOtherUserId)) {
                followUserButton.setText("Following ✓");
            } else {
                followUserButton.setText("Follow");
            }
        } catch (Exception ex) {
            logger.error("Error checking follow status", ex);
            followUserButton.setText("Follow"); // fallback
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Search
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty())
            return;
        // TODO: implement search across posts, communities, users
        logger.info("Search query: {}", query);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Logout
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception ex) {
            logger.error("Error navigating to login", ex);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Makes one view VBox visible+managed and hides all others.
     */
    private void activateView(VBox target) {
        for (VBox view : new VBox[] { homeView, communitiesView, createStyleView,
                profileView, settingsView,
                communityDetailView, otherProfileView }) {
            boolean active = view == target;
            view.setVisible(active);
            view.setManaged(active);
        }
    }

    /**
     * Marks {@code active} as the highlighted sidebar button and resets all others.
     */
    private void setNavActive(Button active) {
        for (Button btn : new Button[] { homeButton, communitiesButton,
                createStyleButton, profileButton, settingsButton }) {
            btn.setStyle(btn == active ? NAV_ACTIVE : NAV_INACTIVE);
        }
    }

    /**
     * Builds a simple post card {@link Node} from a {@link Post}.
     *
     * <p>
     * Replace with an FXMLLoader call loading {@code post.fxml} once the
     * Post component controller (PostController) is wired up; for now this
     * returns a styled VBox placeholder.
     */
    private Node buildPostCard(Post post) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #C0B7AD; -fx-background-radius: 15; -fx-padding: 12;");
        card.setMaxWidth(Double.MAX_VALUE);

        Label author = new Label("by @" + post.getAuthorUsername());
        author.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a4a4a;");
        // Clicking the author label navigates to their profile
        author.setOnMouseClicked(e -> openOtherProfile(post.getAuthorId()));
        author.setStyle(author.getStyle() + " -fx-cursor: hand;");

        AnchorPane postOutfitPreview = new AnchorPane();
        postOutfitPreview.setPrefWidth(220);
        postOutfitPreview.setPrefHeight(340);
        postOutfitPreview.setStyle("-fx-background-color: #f8f8f6; -fx-border-color: #b9b2ab; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12;");

        renderOutfitPreview(postOutfitPreview, toCategoryMap(post.getClothingItems()), false);
        if (postOutfitPreview.getChildren().isEmpty()) {
            Label noItemsLabel = infoLabel("No outfit items attached.");
            noItemsLabel.setStyle("-fx-text-fill: #8a847e; -fx-font-size: 13px;");
            noItemsLabel.setLayoutX(20);
            noItemsLabel.setLayoutY(150);
            postOutfitPreview.getChildren().add(noItemsLabel);
        }

        card.getChildren().addAll(author, postOutfitPreview);
        return card;
    }

    private Map<ClothingCategory, ClothingItem> toCategoryMap(List<ClothingItem> items) {
        Map<ClothingCategory, ClothingItem> itemsByCategory = new EnumMap<>(ClothingCategory.class);
        if (items == null) {
            return itemsByCategory;
        }

        for (ClothingItem item : items) {
            if (item == null || item.getCategory() == null) {
                continue;
            }

            itemsByCategory.putIfAbsent(item.getCategory(), item);
        }

        return itemsByCategory;
    }

    /** Styled info/placeholder label. */
    private Label infoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 10;");
        label.setWrapText(true);
        return label;
    }

    /**
     * Section-header label matching the brown banner style used throughout the app.
     */
    private Label sectionHeader(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setPrefHeight(44);
        label.setStyle(
                "-fx-background-color: #745a42; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 28px;");
        return label;
    }
}
