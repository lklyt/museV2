package com.muse.service;

import com.muse.dao.PostDAO;
import com.muse.dao.PostDAOImpl;
import com.muse.models.ClothingItem;
import com.muse.models.Post;

import java.util.List;
import java.util.Optional;

public class PostService {
    private final PostDAO postDAO = new PostDAOImpl();

    public Post createPost(int authorId, int communityId) throws Exception {

        Post post = new Post(authorId, communityId);
        return postDAO.save(post);
    }

    public Post createPost(int authorId, int communityId, List<ClothingItem> clothingItems) throws Exception {
        Post post = new Post(authorId, communityId);
        post.setClothingItems(clothingItems);
        return postDAO.save(post);
    }

    public Optional<Post> getPostById(int postId) throws Exception {
        return postDAO.findById(postId);
    }

    public List<Post> getPostsByCommunity(int communityId) throws Exception {
        return postDAO.findByCommunityId(communityId);
    }

    public List<Post> getPostsByAuthor(int authorId) throws Exception {
        return postDAO.findByAuthorId(authorId);
    }

    public List<Post> getAllPosts() throws Exception {
        return postDAO.findAll();
    }

    public boolean deletePost(int postId) throws Exception {
        return postDAO.delete(postId);
    }

    // --- RATING SYSTEM METHODS ---

    /**
     * Updates a post object with its current average rating and the specific user's rating.
     * This allows us to keep the existing 'getAllPosts' signature unchanged.
     */
    public void loadRatingData(Post post, int userId) {
        try {
            post.setAverageRating(postDAO.getAverageRating(post.getPostId()));
            post.setUserRating(postDAO.getUserRating(post.getPostId(), userId));
        } catch (Exception e) {
            // Silently fail or log - we don't want to crash the feed if 1 rating fails
            System.err.println("Error loading ratings for post " + post.getPostId() + ": " + e.getMessage());
        }
    }

    /**
     * Saves a new rating and returns the updated average for the UI to display.
     */
    public double ratePost(int postId, int userId, int rating) throws Exception {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be 1-5");
        
        postDAO.ratePost(postId, userId, rating);
        return postDAO.getAverageRating(postId);
    }

    /**
     * Just gets the average for a specific post (useful for top-right label updates).
     */
    public double getAverageRating(int postId) throws Exception {
        return postDAO.getAverageRating(postId);
    }

    // --- SAVED POSTS METHODS ---

    /**
     * Updates a post object with whether the current user has saved it.
     * This follows the same pattern as loadRatingData for the rating system.
     */
    public void loadSaveStatus(Post post, int userId) {
        try {
            post.setSavedByCurrentUser(postDAO.isSaved(post.getPostId(), userId));
        } catch (Exception e) {
            System.err.println("Error loading save status for post " + post.getPostId() + ": " + e.getMessage());
        }
    }

    /**
     * Saves a post for the current user.
     */
    public void savePost(int postId, int userId) throws Exception {
        postDAO.savePost(postId, userId);
    }

    /**
     * Removes a saved post for the current user.
     */
    public void unsavePost(int postId, int userId) throws Exception {
        postDAO.unsavePost(postId, userId);
    }

    /**
     * Checks if a post is saved by the current user.
     */
    public boolean isSaved(int postId, int userId) throws Exception {
        return postDAO.isSaved(postId, userId);
    }

    /**
     * Gets all posts saved by the current user.
     */
    public List<Post> getSavedPosts(int userId) throws Exception {
        return postDAO.getSavedPostsByUserId(userId);
    }



}
