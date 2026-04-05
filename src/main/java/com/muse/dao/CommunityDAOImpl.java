package com.muse.dao;

import com.muse.config.DatabaseConfig;
import com.muse.models.Community;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommunityDAOImpl implements CommunityDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommunityDAOImpl.class);

    @Override
    public Community save(Community community) throws Exception {
        String sql = "INSERT INTO communities (name, post_count) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, community.getName());
            stmt.setInt(2, community.getPostCount());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating community failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    community.setCommunityId(generatedKeys.getInt(1));
                    community.setCreatedAt(LocalDateTime.now());
                    community.setUpdatedAt(LocalDateTime.now());
                    logger.info("Community created with ID: " + community.getCommunityId());
                }
            }
        }
        return community;
    }

    @Override
    public Optional<Community> findById(int communityId) throws Exception {
        String sql = "SELECT * FROM communities WHERE community_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCommunity(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Community> findByName(String name) throws Exception {
        String sql = "SELECT * FROM communities WHERE name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCommunity(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Community> findAll() throws Exception {
        List<Community> communities = new ArrayList<>();
        String sql = "SELECT * FROM communities ORDER BY created_at DESC LIMIT 1000";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                communities.add(mapResultSetToCommunity(rs));
            }
        }
        return communities;
    }

    @Override
    public boolean update(Community community) throws Exception {
        String sql = "UPDATE communities SET name = ?, post_count = ? WHERE community_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, community.getName());
            stmt.setInt(2, community.getPostCount());
            stmt.setInt(3, community.getCommunityId());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int communityId) throws Exception {
        String sql = "DELETE FROM communities WHERE community_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Community mapResultSetToCommunity(ResultSet rs) throws SQLException {
        Community community = new Community();
        community.setCommunityId(rs.getInt("community_id"));
        community.setName(rs.getString("name"));
        community.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        community.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        community.setPostCount(rs.getInt("post_count"));
        return community;
    }
}
