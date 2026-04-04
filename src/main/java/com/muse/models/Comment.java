package com.muse.models;

import java.time.LocalDateTime;

public class Comment {
    private int commentId;
    private int postId;
    private int authorId;
    private String authorUsername;
    private String content;
    private int likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Comment() {}

    public Comment(int postId, int authorId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.likesCount = 0;
    }

    // Getters and Setters
    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
