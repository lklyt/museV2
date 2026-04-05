package com.muse.dao;

import com.muse.models.Comment;
import java.util.Optional;
import java.util.List;

public interface CommentDAO {
    Comment save(Comment comment) throws Exception;
    Optional<Comment> findById(int commentId) throws Exception;
    List<Comment> findByPostId(int postId) throws Exception;
    List<Comment> findByAuthorId(int authorId) throws Exception;
    boolean update(Comment comment) throws Exception;
    boolean delete(int commentId) throws Exception;
}
