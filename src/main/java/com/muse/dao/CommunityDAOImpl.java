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
        String sql = "INSERT INTO communities (name, description, creator_id, icon_url) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, community.getName());
            stmt.setString(2, community.getDescription());
            stmt.setInt(3, community.getCreatorId());
            stmt.setString(4, community.getIconUrl());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating community failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    community.setCommunityId(generatedKeys.getInt(1));
                    community.setCreatedAt(LocalDateTime.now());
                    community.setUpdatedAt(LocalDateTime.now());
                    addMember(community.getCommunityId(), community.getCreatorId());
                    logger.info("Community created with ID: " + community.getCommunityId());
                }
            }
        }
        return community;
    }

    @Override
    public Optional<Community> findById(int communityId) throws Exception {
        String sql = "SELECT c.*, u.username FROM communities c " +
                     "LEFT JOIN users u ON c.creator_id = u.user_id WHERE c.community_id = ?";

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
        String sql = "SELECT c.*, u.username FROM communities c " +
                     "LEFT JOIN users u ON c.creator_id = u.user_id WHERE c.name = ?";

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
        String sql = "SELECT c.*, u.username FROM communities c " +
                     "LEFT JOIN users u ON c.creator_id = u.user_id ORDER BY c.created_at DESC LIMIT 1000";

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
    public List<Community> findByMemberId(int userId) throws Exception {
        List<Community> communities = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM communities c " +
                     "LEFT JOIN users u ON c.creator_id = u.user_id " +
                     "JOIN community_members cm ON c.community_id = cm.community_id " +
                     "WHERE cm.user_id = ? ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    communities.add(mapResultSetToCommunity(rs));
                }
            }
        }
        return communities;
    }

    @Override
    public boolean update(Community community) throws Exception {
        String sql = "UPDATE communities SET name = ?, description = ?, icon_url = ? WHERE community_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, community.getName());
            stmt.setString(2, community.getDescription());
            stmt.setString(3, community.getIconUrl());
            stmt.setInt(4, community.getCommunityId());

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

    @Override
    public boolean addMember(int communityId, int userId) throws Exception {
        String sql = "INSERT IGNORE INTO community_members (community_id, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            // Update member count
            String updateCountSql = "UPDATE communities SET member_count = (SELECT COUNT(*) FROM community_members WHERE community_id = ?) WHERE community_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCountSql)) {
                updateStmt.setInt(1, communityId);
                updateStmt.setInt(2, communityId);
                updateStmt.executeUpdate();
            }
            return true;
        }
    }

    @Override
    public boolean removeMember(int communityId, int userId) throws Exception {
        String sql = "DELETE FROM community_members WHERE community_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            // Update member count
            String updateCountSql = "UPDATE communities SET member_count = (SELECT COUNT(*) FROM community_members WHERE community_id = ?) WHERE community_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCountSql)) {
                updateStmt.setInt(1, communityId);
                updateStmt.setInt(2, communityId);
                updateStmt.executeUpdate();
            }
            return true;
        }
    }

    @Override
    public boolean isMember(int communityId, int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM community_members WHERE community_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
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
    public int getMemberCount(int communityId) throws Exception {
        String sql = "SELECT COUNT(*) FROM community_members WHERE community_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Community mapResultSetToCommunity(ResultSet rs) throws SQLException {
        Community community = new Community();
        community.setCommunityId(rs.getInt("community_id"));
        community.setName(rs.getString("name"));
        community.setDescription(rs.getString("description"));
        community.setCreatorId(rs.getInt("creator_id"));
        community.setCreatorUsername(rs.getString("username"));
        community.setIconUrl(rs.getString("icon_url"));
        community.setMemberCount(rs.getInt("member_count"));
        community.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        community.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return community;
    }
}
