package com.muse.models;

import javafx.scene.image.Image;

public class ClothingItem {
    private int id;
    private ClothingCategory category;
    private String description;
    private Image image;

    public ClothingItem() {}

    public ClothingItem(ClothingCategory category, String description, Image image) {
        this.category = category;
        this.description = description;
        this.image = image;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ClothingCategory getCategory() { return category; }
    public void setCategory(ClothingCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
}
