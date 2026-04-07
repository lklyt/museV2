package com.muse.service;

import com.muse.dao.PostDAO;
import com.muse.dao.UserDAO;
import com.muse.models.Post;
import com.muse.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FeedService {

    private PostDAO postDAO;
    private UserDAO userDAO;

    // Bayesian average tuning constant.
    // Represents the minimum number of ratings a post needs before its average score is fully trusted. 
    private static final int MINIMUM_RATINGS_THRESHOLD = 10;

    public FeedService(PostDAO postDAO, UserDAO userDAO) {
        this.postDAO = postDAO;
        this.userDAO = userDAO;
    }

    /**
     * Returns posts from accounts the current user follows.
     * Ordered from newest to oldest (chronological).
     *
     * @param currentUserId the logged-in user's database ID
     * @return ordered list of posts from followed accounts
     */
    public List<Post> getForYouFeed(int currentUserId) throws Exception {
        ArrayList<Post> result = new ArrayList<Post>();

        List<User> following = userDAO.getFollowing(currentUserId);

        if (following.isEmpty()) {
            return result;
        }

        ArrayList<Integer> followingIds = new ArrayList<Integer>();
        for (int i = 0; i < following.size(); i++) {
            followingIds.add(following.get(i).getUserId());
        }

        List<Post> allPosts = postDAO.findAll();

        for (int i = 0; i < allPosts.size(); i++) {
            Post currentPost = allPosts.get(i);
            if (followingIds.contains(currentPost.getAuthorId())) {
                result.add(currentPost);
            }
        }

        Collections.sort(result, new Comparator<Post>() {
            public int compare(Post first, Post second) {
                LocalDateTime firstTime  = first.getCreatedAt();
                LocalDateTime secondTime = second.getCreatedAt();

                if (firstTime == null && secondTime == null) {
                    return 0;
                }
                if (firstTime == null) {
                    return 1;
                }
                if (secondTime == null) {
                    return -1;
                }

                return secondTime.compareTo(firstTime); // newest first
            }
        });

        return result;
    }

    /**
     * Returns all posts ranked for the Discover page.
     *
     * Uses a Bayesian weighted average to rank posts, which balances
     * rating score against rating count. This prevents a post with a
     * single 5-star rating from outranking a post with hundreds of
     * high ratings.
     *
     * Formula:
     *   weighted = (ratingCount / (ratingCount + C)) * averageRating
     *            + (C / (ratingCount + C)) * globalAverage
     *
     * Where C = MINIMUM_RATINGS_THRESHOLD and globalAverage is the
     * mean rating across all posts in the feed.
     *
     * Posts with equal weighted scores are broken by recency (newest first).
     *
     * @param currentUserId the logged-in user's database ID (reserved for
     *                      future filtering, e.g. excluding own posts)
     * @return ranked list of posts for the Discover feed
     */
    public List<Post> getDiscoverFeed(int currentUserId) throws Exception {
        ArrayList<Post> result = new ArrayList<Post>();

        List<Post> allPosts = postDAO.findAll();

        for (int i = 0; i < allPosts.size(); i++) {
            result.add(allPosts.get(i));
        }

        if (result.isEmpty()) {
            return result;
        }

        final HashMap<Integer, Double>  averageRatings = new HashMap<Integer, Double>();
        final HashMap<Integer, Integer> ratingCounts   = new HashMap<Integer, Integer>();

        for (int i = 0; i < result.size(); i++) {
            Post currentPost = result.get(i);
            int  postId      = currentPost.getPostId();

            double averageRating = postDAO.getAverageRating(postId);
            int    ratingCount   = postDAO.getRatingCount(postId);

            averageRatings.put(postId, averageRating);
            ratingCounts.put(postId, ratingCount);
        }

        // Calculate global average rating across all posts that have been rated
        double globalAverage = calculateGlobalAverage(result, averageRatings, ratingCounts);

        // Calculate Bayesian weighted score for each post
        final HashMap<Integer, Double> weightedScores = new HashMap<Integer, Double>();

        for (int i = 0; i < result.size(); i++) {
            Post   currentPost   = result.get(i);
            int    postId        = currentPost.getPostId();
            double averageRating = averageRatings.get(postId);
            int    ratingCount   = ratingCounts.get(postId);

            double weightedScore = calculateBayesianScore(
                    averageRating, ratingCount, globalAverage);

            weightedScores.put(postId, weightedScore);
        }

        // Sort by weighted score descending, then by recency descending
        Collections.sort(result, new Comparator<Post>() {
            public int compare(Post first, Post second) {
                double firstScore  = 0.0;
                double secondScore = 0.0;

                if (weightedScores.containsKey(first.getPostId())) {
                    firstScore = weightedScores.get(first.getPostId());
                }
                if (weightedScores.containsKey(second.getPostId())) {
                    secondScore = weightedScores.get(second.getPostId());
                }

                if (secondScore != firstScore) {
                    // Higher score ranks first
                    if (secondScore > firstScore) {
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }

                LocalDateTime firstTime  = first.getCreatedAt();
                LocalDateTime secondTime = second.getCreatedAt();

                if (firstTime == null && secondTime == null) {
                    return 0;
                }
                if (firstTime == null) {
                    return 1;
                }
                if (secondTime == null) {
                    return -1;
                }

                return secondTime.compareTo(firstTime);
            }
        });

        return result;
    }

    /**
     * Calculates the Bayesian weighted score for a single post.
     *
     * @param averageRating the post's mean star rating (0.0 – 5.0)
     * @param ratingCount   how many users have rated this post
     * @param globalAverage mean rating across all posts in the feed
     * @return Bayesian weighted score (0.0 – 5.0)
     */
    private double calculateBayesianScore(double averageRating,
                                          int    ratingCount,
                                          double globalAverage) {
        int C = MINIMUM_RATINGS_THRESHOLD;

        double weightFromRatings = (double) ratingCount / (ratingCount + C);
        double weightFromPrior   = (double) C           / (ratingCount + C);

        return (weightFromRatings * averageRating) + (weightFromPrior * globalAverage);
    }

    private double calculateGlobalAverage(ArrayList<Post>             posts,
                                          HashMap<Integer, Double>    averageRatings,
                                          HashMap<Integer, Integer>   ratingCounts) {
        double totalRatingSum   = 0.0;
        int    ratedPostsCount  = 0;

        for (int i = 0; i < posts.size(); i++) {
            int postId      = posts.get(i).getPostId();
            int ratingCount = 0;

            if (ratingCounts.containsKey(postId)) {
                ratingCount = ratingCounts.get(postId);
            }

            if (ratingCount > 0) {
                totalRatingSum  = totalRatingSum + averageRatings.get(postId);
                ratedPostsCount = ratedPostsCount + 1;
            }
        }

        if (ratedPostsCount == 0) {
            return 3.0; // neutral midpoint fallback when nothing is rated yet
        }

        return totalRatingSum / ratedPostsCount;
    }
}