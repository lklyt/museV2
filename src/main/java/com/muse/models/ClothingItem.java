package com.muse.models;


public class ClothingItem {
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
}
