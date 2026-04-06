package com.muse.models;

import com.google.gson.annotations.SerializedName;

public class ImageMetadata {
    @SerializedName("itemId")
    private int itemId;

    @SerializedName("category")
    private String category;

    @SerializedName("originalUrl")
    private String originalUrl;

    @SerializedName("cachedAt")
    private long cachedAt;

    @SerializedName("lastAccessedAt")
    private long lastAccessedAt;

    @SerializedName("fileSize")
    private long fileSize;

    @SerializedName("expiresAt")
    private long expiresAt;

    @SerializedName("etag")
    private String etag;

    @SerializedName("fileName")
    private String fileName;

    public ImageMetadata() {}

    public ImageMetadata(int itemId, String category, String originalUrl, long fileSize, String etag) {
        this.itemId = itemId;
        this.category = category;
        this.originalUrl = originalUrl;
        this.fileSize = fileSize;
        this.etag = etag;
        this.cachedAt = System.currentTimeMillis();
        this.lastAccessedAt = System.currentTimeMillis();
        this.expiresAt = cachedAt + (30L * 24 * 60 * 60 * 1000);  // 30 days default
    }

    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }

    public long getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(long lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    /**
     * Check if this cached entry has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    /**
     * Update last accessed time to current
     */
    public void updateLastAccessedTime() {
        this.lastAccessedAt = System.currentTimeMillis();
    }
}
