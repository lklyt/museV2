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
        String sql = "INSERT INTO users (username, email, password_hash, display_name, bio, profile_picture_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getDisplayName());
            stmt.setString(5, user.getBio());
            stmt.setString(6, user.getProfilePictureUrl());

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
        String sql = "UPDATE users SET username = ?, email = ?, display_name = ?, bio = ?, profile_picture_url = ? " +
                     "WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getDisplayName());
            stmt.setString(4, user.getBio());
            stmt.setString(5, user.getProfilePictureUrl());
            stmt.setInt(6, user.getUserId());

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
        user.setDisplayName(rs.getString("display_name"));
        user.setBio(rs.getString("bio"));
        user.setProfilePictureUrl(rs.getString("profile_picture_url"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }
}
