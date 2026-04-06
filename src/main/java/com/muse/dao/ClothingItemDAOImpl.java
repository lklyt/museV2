package com.muse.dao;

import com.muse.config.DatabaseConfig;
import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClothingItemDAOImpl implements ClothingItemDAO {
    private static final Logger logger = LoggerFactory.getLogger(ClothingItemDAOImpl.class);

    @Override
    public ClothingItem save(ClothingItem item) throws Exception {
        String sql = "INSERT INTO clothing_items (name, item_category, image_url) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getDescription());
            stmt.setString(2, item.getCategory().toString().toUpperCase());
            stmt.setString(3, item.getImageUrl());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating clothing item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                    logger.info("Clothing item created with ID: " + item.getId());
                }
            }
        }
        return item;
    }

    @Override
    public ClothingItem findById(int id) throws Exception {
        String sql = "SELECT * FROM clothing_items WHERE item_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClothingItem(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<ClothingItem> findByCategory(ClothingCategory category) throws Exception {
        List<ClothingItem> items = new ArrayList<>();
        String sql = "SELECT * FROM clothing_items WHERE item_category = ? ORDER BY name ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToClothingItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<ClothingItem> findAll() throws Exception {
        List<ClothingItem> items = new ArrayList<>();
        String sql = "SELECT * FROM clothing_items ORDER BY item_category, name ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToClothingItem(rs));
            }
        }
        return items;
    }

    private ClothingItem mapResultSetToClothingItem(ResultSet rs) throws SQLException {
        ClothingItem item = new ClothingItem();
        item.setId(rs.getInt("item_id"));
        item.setDescription(rs.getString("name"));
        item.setCategory(ClothingCategory.valueOf(rs.getString("item_category").toUpperCase()));
        item.setImageUrl(rs.getString("image_url"));
        return item;
    }
}
