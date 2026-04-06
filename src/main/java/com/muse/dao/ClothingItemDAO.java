package com.muse.dao;

import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;
import java.util.List;

public interface ClothingItemDAO {
    ClothingItem save(ClothingItem item) throws Exception;
    ClothingItem findById(int id) throws Exception;
    List<ClothingItem> findByCategory(ClothingCategory category) throws Exception;
    List<ClothingItem> findAll() throws Exception;
}
