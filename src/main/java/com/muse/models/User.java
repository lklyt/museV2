package com.muse.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Searchable {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<User> following;
    private List<User> followers;

    public User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<User> getFollowing() { return following; }
    public void setFollowing(List<User> following) { this.following = following; }

    public List<User> getFollowers() { return followers; }
    public void setFollowers(List<User> followers) { this.followers = followers; }

    @Override
    public ArrayList<String> getSearchKeywords() {
        ArrayList<String> keywords = new ArrayList<>();
        if (username != null) keywords.add(username); // index 0 – primary
        return keywords;
    }

    @Override
    public SearchType getSearchType() {
        return SearchType.PEOPLE;
    }
}
