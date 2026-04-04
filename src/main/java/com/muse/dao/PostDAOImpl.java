package com.muse.dao;

import com.muse.config.DatabaseConfig;
import com.muse.models.Post;
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
        String sql = "INSERT INTO posts (author_id, community_id, title, content, likes_count, comments_count) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, post.getAuthorId());
            stmt.setInt(2, post.getCommunityId());
            stmt.setString(3, post.getTitle());
            stmt.setString(4, post.getContent());
            stmt.setInt(5, post.getLikesCount());
            stmt.setInt(6, post.getCommentsCount());

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
    public boolean update(Post post) throws Exception {
        String sql = "UPDATE posts SET title = ?, content = ? WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setInt(3, post.getPostId());

            return stmt.executeUpdate() > 0;
        }
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

    @Override
    public boolean incrementLikes(int postId) throws Exception {
        String sql = "UPDATE posts SET likes_count = likes_count + 1 WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean decrementLikes(int postId) throws Exception {
        String sql = "UPDATE posts SET likes_count = CASE WHEN likes_count > 0 THEN likes_count - 1 ELSE 0 END WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean incrementComments(int postId) throws Exception {
        String sql = "UPDATE posts SET comments_count = comments_count + 1 WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean decrementComments(int postId) throws Exception {
        String sql = "UPDATE posts SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END WHERE post_id = ?";

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
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setLikesCount(rs.getInt("likes_count"));
        post.setCommentsCount(rs.getInt("comments_count"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return post;
    }
}
