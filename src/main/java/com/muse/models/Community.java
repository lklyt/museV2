package com.muse.models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Community implements Searchable {
    private int communityId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int postCount = 0;

    public Community() {}

    public Community(String name) {
        this.name = name;
    }

    // Getters and Setters
    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int count) { this.postCount = count; }

    // Implementing Searchable
    /**
     * Index 0: community name → primary identifier shown in suggestions.
     */
    @Override
    public ArrayList<String> getSearchKeywords() {
        ArrayList<String> keywords = new ArrayList<>();
        if (name != null) keywords.add(name); // index 0 – primary
        return keywords;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.COMMUNITIES;
    }
}