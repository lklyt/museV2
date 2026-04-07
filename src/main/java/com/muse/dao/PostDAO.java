package com.muse.dao;

import com.muse.models.Post;
import java.util.Optional;
import java.util.List;

public interface PostDAO {
    Post save(Post post) throws Exception;
    Optional<Post> findById(int postId) throws Exception;
    List<Post> findByCommunityId(int communityId) throws Exception;
    List<Post> findByAuthorId(int authorId) throws Exception;
    List<Post> findAll() throws Exception;
    boolean delete(int postId) throws Exception;
    void ratePost(int postId, int userId, int rating) throws Exception;
    double getAverageRating(int postId) throws Exception;
    int getUserRating(int postId, int userId) throws Exception;
    void savePost(int postId, int userId) throws Exception;
    void unsavePost(int postId, int userId) throws Exception;
    boolean isSaved(int postId, int userId) throws Exception;
    List<Post> getSavedPostsByUserId(int userId) throws Exception;
}
