package com.muse.models;

import java.util.ArrayList;

public class ClothingItem implements Searchable {
    private int id;
    private ClothingCategory category;
    private String description;
    private String imageUrl;

    public ClothingItem() {}

    public ClothingItem(ClothingCategory category, String description, String imageUrl) {
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ClothingCategory getCategory() { return category; }
    public void setCategory(ClothingCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Implementing Searchable
    /**
     * Index 0: description → the item's name/description is the primary
     *          identifier shown in autocomplete (e.g. "Black Leather Jacket").
     * Index 1: category name → lets queries like "shoes" or "tops" match items
     *          even when the description doesn't contain the category word.
     */
    @Override
    public ArrayList<String> getSearchKeywords() {
        ArrayList<String> keywords = new ArrayList<>();

        // Index 0 – primary identifier
        if (description != null) keywords.add(description);

        // Index 1 – category as a supporting keyword
        if (category != null) keywords.add(category.name());

        return keywords;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.CLOTHING_ITEMS;
    }
}