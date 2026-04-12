package com.muse.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Post implements Searchable {
    private int postId;
    private int authorId;
    private String authorUsername;
    private int communityId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Comment> comments;
    private List<ClothingItem> clothingItems;
    private double averageRating;
    private int userRating;
    private boolean isSavedByCurrentUser;

    public Post() {
        this.comments = new ArrayList<>();
        this.clothingItems = new ArrayList<>();
    }

    public Post(int authorId, int communityId) {
        this.authorId = authorId;
        this.communityId = communityId;
        this.createdAt = LocalDateTime.now();
        this.comments = new ArrayList<>();
        this.clothingItems = new ArrayList<>();
    }

    // Getters and Setters
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<ClothingItem> getClothingItems() { return clothingItems; }
    public void setClothingItems(List<ClothingItem> clothingItems) { this.clothingItems = clothingItems; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getUserRating() { return userRating; }
    public void setUserRating(int userRating) { this.userRating = userRating; }

    public boolean isSavedByCurrentUser() { return isSavedByCurrentUser; }
    public void setSavedByCurrentUser(boolean isSavedByCurrentUser) { this.isSavedByCurrentUser = isSavedByCurrentUser; }

        @Override
    public ArrayList<String> getSearchKeywords() {
        ArrayList<String> keywords = new ArrayList<>();

        // Index 0 – primary
        if (authorUsername != null) keywords.add(authorUsername);

        // Index 1 – clothing items attached to this post act as content tags
        if (clothingItems != null) {
            for (ClothingItem item : clothingItems) {
                if (item.getDescription() != null) {
                    keywords.add(item.getDescription());
                }

                if (item.getCategory() != null) {
                    keywords.add(item.getCategory().name());
                }
            }
        }

        return keywords;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.POSTS;
    }
}
