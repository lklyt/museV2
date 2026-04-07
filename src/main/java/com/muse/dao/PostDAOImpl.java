package com.muse.dao;

import com.muse.config.DatabaseConfig;
import com.muse.models.Post;
import com.muse.models.Comment;
import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostDAOImpl implements PostDAO {
    private static final Logger logger = LoggerFactory.getLogger(PostDAOImpl.class);

    @Override
    public Post save(Post post) throws Exception {
        String sql = "INSERT INTO posts (author_id, community_id) " +
                     "VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, post.getAuthorId());
            stmt.setInt(2, post.getCommunityId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating post failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setPostId(generatedKeys.getInt(1));
                    post.setCreatedAt(LocalDateTime.now());
                    post.setUpdatedAt(LocalDateTime.now());
                    logger.info("Post created with ID: " + post.getPostId());
                }
            }

            // Save associated clothing items if any
            if (post.getClothingItems() != null && !post.getClothingItems().isEmpty()) {
                saveClothingItems(post);
            }
        }
        return post;
    }

    @Override
    public Optional<Post> findById(int postId) throws Exception {
        String sql = "SELECT p.*, u.username FROM posts p " +
                     "JOIN users u ON p.author_id = u.user_id WHERE p.post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPost(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Post> findByCommunityId(int communityId) throws Exception {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM posts p " +
                     "JOIN users u ON p.author_id = u.user_id WHERE p.community_id = ? " +
                     "ORDER BY p.created_at DESC LIMIT 1000";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }  
        }
        return posts;
    }

    @Override
    public List<Post> findByAuthorId(int authorId) throws Exception {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM posts p " +
                     "JOIN users u ON p.author_id = u.user_id WHERE p.author_id = ? " +
                     "ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }

    @Override
    public List<Post> findAll() throws Exception {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM posts p " +
                     "JOIN users u ON p.author_id = u.user_id ORDER BY p.created_at DESC LIMIT 1000";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        }
        return posts;
    }


    @Override
    public boolean delete(int postId) throws Exception {
        String sql = "DELETE FROM posts WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        }
    }

  

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setPostId(rs.getInt("post_id"));
        post.setAuthorId(rs.getInt("author_id"));
        post.setAuthorUsername(rs.getString("username"));
        post.setCommunityId(rs.getInt("community_id"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        // Load clothing items for this post
        try {
            List<ClothingItem> clothingItems = loadClothingItemsByPostId(rs.getInt("post_id"));
            post.setClothingItems(clothingItems);
        } catch (Exception e) {
            logger.warn("Failed to load clothing items for post " + rs.getInt("post_id"), e);
            post.setClothingItems(new ArrayList<>());
        }

        // Load comments for this post
        try {
            List<Comment> comments = loadCommentsByPostId(rs.getInt("post_id"));
            post.setComments(comments);
        } catch (Exception e) {
            logger.warn("Failed to load comments for post " + rs.getInt("post_id"), e);
            post.setComments(new ArrayList<>());
        }

        // Load average rating for this post
        try {
            double avgRating = getAverageRating(rs.getInt("post_id"));
            post.setAverageRating(avgRating);
        } catch (Exception e) {
            logger.warn("Failed to load average rating for post " + rs.getInt("post_id"), e);
            post.setAverageRating(0.0);
        }

        return post;
    }

    private List<Comment> loadCommentsByPostId(int postId) throws Exception {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c " +
                     "JOIN users u ON c.author_id = u.user_id WHERE c.post_id = ? " +
                     "ORDER BY c.created_at ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(rs.getInt("comment_id"));
                    comment.setPostId(rs.getInt("post_id"));
                    comment.setAuthorId(rs.getInt("author_id"));
                    comment.setAuthorUsername(rs.getString("username"));
                    comment.setContent(rs.getString("content"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    comment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    private List<ClothingItem> loadClothingItemsByPostId(int postId) throws Exception {
        List<ClothingItem> items = new ArrayList<>();
        String sql = "SELECT ci.item_id, ci.name, ci.item_category, ci.image_url " +
                     "FROM clothing_items ci " +
                     "JOIN post_clothing_items pci ON ci.item_id = pci.item_id " +
                     "WHERE pci.post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClothingItem item = new ClothingItem();
                    item.setId(rs.getInt("item_id"));
                    item.setDescription(rs.getString("name"));
                    item.setCategory(ClothingCategory.valueOf(rs.getString("item_category").toUpperCase()));
                    item.setImageUrl(rs.getString("image_url"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private void saveClothingItems(Post post) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            for (ClothingItem item : post.getClothingItems()) {
                // First, insert or get the clothing item ID
                int itemId = insertOrGetClothingItem(conn, item);
                
                // Then, create the relationship in the junction table
                String linkSql = "INSERT INTO post_clothing_items (post_id, item_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(linkSql)) {
                    stmt.setInt(1, post.getPostId());
                    stmt.setInt(2, itemId);
                    stmt.executeUpdate();
                }
            }
            logger.info("Saved " + post.getClothingItems().size() + " clothing item associations for post " + post.getPostId());
        }
    }

    private int insertOrGetClothingItem(Connection conn, ClothingItem item) throws SQLException {
        // Try to find existing item by name and category
        String selectSql = "SELECT item_id FROM clothing_items WHERE name = ? AND item_category = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, item.getDescription());
            selectStmt.setString(2, item.getCategory().toString());
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("item_id");
                }
            }
        }
        
        // Insert new clothing item if it doesn't exist
        String insertSql = "INSERT INTO clothing_items (name, item_category, image_url) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, item.getDescription());
            insertStmt.setString(2, item.getCategory().toString());
            insertStmt.setString(3, item.getImageUrl());
            
            insertStmt.executeUpdate();
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        
        throw new SQLException("Failed to insert or retrieve clothing item");
    }

    @Override
    public void ratePost(int postId, int userId, int rating) throws Exception {
        // ON DUPLICATE KEY UPDATE ensures if a user clicks a different star, it updates their old rating
        String sql = "INSERT INTO post_stars (post_id, user_id, rating) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE rating = VALUES(rating)";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setInt(3, rating);
            stmt.executeUpdate();
        }
    }

    @Override
    public double getAverageRating(int postId) throws Exception {
        String sql = "SELECT AVG(rating) FROM post_stars WHERE post_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    @Override
    public int getUserRating(int postId, int userId) throws Exception {
        String sql = "SELECT rating FROM post_stars WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rating");
                }
            }
        }
        return 0; // Return 0 if the user hasn't rated this post yet
    }

    @Override
    public void savePost(int postId, int userId) throws Exception {
        String sql = "INSERT INTO saved_posts (post_id, user_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE created_at = created_at";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            logger.info("Post " + postId + " saved by user " + userId);
        }
    }

    @Override
    public void unsavePost(int postId, int userId) throws Exception {
        String sql = "DELETE FROM saved_posts WHERE post_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            logger.info("Post " + postId + " unsaved by user " + userId);
        }
    }

    @Override
    public boolean isSaved(int postId, int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM saved_posts WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<Post> getSavedPostsByUserId(int userId) throws Exception {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM posts p " +
                     "JOIN users u ON p.author_id = u.user_id " +
                     "JOIN saved_posts sp ON p.post_id = sp.post_id " +
                     "WHERE sp.user_id = ? ORDER BY sp.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        }
        return posts;
    }
}
