package com.muse.service;

import com.muse.dao.ClothingItemDAO;
import com.muse.dao.ClothingItemDAOImpl;
import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;
import com.muse.util.ImageCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClothingItemService {
    private static final Logger logger = LoggerFactory.getLogger(ClothingItemService.class);
    private final ClothingItemDAO clothingItemDAO = new ClothingItemDAOImpl();
    private final ImageCacheManager cacheManager = ImageCacheManager.getInstance();

    public ClothingItem save(ClothingItem item) throws Exception {
        return clothingItemDAO.save(item);
    }

    public ClothingItem findById(int id) throws Exception {
        return clothingItemDAO.findById(id);
    }

    public List<ClothingItem> getItemsByCategory(ClothingCategory category) throws Exception {
        return clothingItemDAO.findByCategory(category);
    }

    /**
     * Get items with cached image paths
     * Lazy loads images on first access, improving performance on subsequent loads
     */
    public List<ClothingItem> getItemsWithCachedImages(ClothingCategory category) throws Exception {
        List<ClothingItem> items = clothingItemDAO.findByCategory(category);
        for (ClothingItem item : items) {
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                String cachedPath = cacheManager.getOrCacheImage(item.getId(), item.getImageUrl(), category);
                if (cachedPath != null) {
                    // cachedPath is already a file:// URL from cache manager
                    item.setImageUrl(cachedPath);
                }
            }
        }
        return items;
    }

    public List<ClothingItem> getAllItems() throws Exception {
        return clothingItemDAO.findAll();
    }
}
