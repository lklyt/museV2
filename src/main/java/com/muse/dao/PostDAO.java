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
    boolean update(Post post) throws Exception;
    boolean delete(int postId) throws Exception;
    boolean incrementLikes(int postId) throws Exception;
    boolean decrementLikes(int postId) throws Exception;
    boolean incrementComments(int postId) throws Exception;
    boolean decrementComments(int postId) throws Exception;
}
