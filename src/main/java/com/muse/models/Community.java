package com.muse.models;

import java.time.LocalDateTime;

public class Community {
    private int communityId;
    private String name;
    private String description;
    private int creatorId;
    private String creatorUsername;
    private String iconUrl;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Community() {}

    public Community(String name, String description, int creatorId) {
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.memberCount = 1;
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

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
