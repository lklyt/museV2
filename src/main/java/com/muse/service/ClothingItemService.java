package com.muse.service;

import com.muse.dao.ClothingItemDAO;
import com.muse.dao.ClothingItemDAOImpl;
import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClothingItemService {
    private static final Logger logger = LoggerFactory.getLogger(ClothingItemService.class);
    private final ClothingItemDAO clothingItemDAO = new ClothingItemDAOImpl();

    public ClothingItem save(ClothingItem item) throws Exception {
        return clothingItemDAO.save(item);
    }

    public ClothingItem findById(int id) throws Exception {
        return clothingItemDAO.findById(id);
    }

    public List<ClothingItem> getItemsByCategory(ClothingCategory category) throws Exception {
        return clothingItemDAO.findByCategory(category);
    }

    public List<ClothingItem> getAllItems() throws Exception {
        return clothingItemDAO.findAll();
    }
}
