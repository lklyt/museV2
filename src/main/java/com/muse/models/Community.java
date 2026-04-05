package com.muse.models;

import java.time.LocalDateTime;

public class Community {
    private int communityId;
    private String name;
    private String description;
    private int creatorId;
    private String creatorUsername;
    private String iconUrl;
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getPostCount(){return postCount;}
    public void setPostCount(int count){this.postCount = count;}
}

