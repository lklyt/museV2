package com.muse.dao;

import com.muse.config.DatabaseConfig;
import com.muse.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public User save(User user) throws Exception {
        String sql = "INSERT INTO users (username, email, password_hash) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    logger.info("User created with ID: " + user.getUserId());
                }
            }
        }
        return user;
    }

    @Override
    public Optional<User> findById(int userId) throws Exception {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws Exception {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users LIMIT 1000";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public boolean update(User user) throws Exception {
        String sql = "UPDATE users SET username = ?, email = ? " +
                     "WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getUserId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(int userId) throws Exception {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean usernameExists(String username) throws Exception {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean emailExists(String email) throws Exception {
        return findByEmail(email).isPresent();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }

    @Override
    public boolean followUser(int followerId, int followingId) throws Exception {
        if (followerId == followingId) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        String sql = "INSERT INTO follows (follower_id, following_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                logger.info("User " + followerId + " already follows user " + followingId);
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean unfollowUser(int followerId, int followingId) throws Exception {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND following_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<User> getFollowers(int userId) throws Exception {
        List<User> followers = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "INNER JOIN follows f ON u.user_id = f.follower_id " +
                     "WHERE f.following_id = ? " +
                     "ORDER BY f.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    followers.add(mapResultSetToUser(rs));
                }
            }
        }
        return followers;
    }

    @Override
    public List<User> getFollowing(int userId) throws Exception {
        List<User> following = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "INNER JOIN follows f ON u.user_id = f.following_id " +
                     "WHERE f.follower_id = ? " +
                     "ORDER BY f.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    following.add(mapResultSetToUser(rs));
                }
            }
        }
        return following;
    }

    @Override
    public boolean isFollowing(int followerId, int followingId) throws Exception {
        String sql = "SELECT 1 FROM follows WHERE follower_id = ? AND following_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int getFollowerCount(int userId) throws Exception {
        String sql = "SELECT COUNT(*) as count FROM follows WHERE following_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    @Override
    public int getFollowingCount(int userId) throws Exception {
        String sql = "SELECT COUNT(*) as count FROM follows WHERE follower_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
}
