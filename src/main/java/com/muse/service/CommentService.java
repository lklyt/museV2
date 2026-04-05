package com.muse.service;

import com.muse.dao.CommentDAO;
import com.muse.dao.CommentDAOImpl;
import com.muse.dao.PostDAO;
import com.muse.dao.PostDAOImpl;
import com.muse.models.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final CommentDAO commentDAO = new CommentDAOImpl();
    private final PostDAO postDAO = new PostDAOImpl();

    public Comment addComment(int postId, int authorId, String content) throws Exception {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        Comment comment = new Comment(postId, authorId, content);
        Comment saved = commentDAO.save(comment);
        return saved;
    }

    public Optional<Comment> getCommentById(int commentId) throws Exception {
        return commentDAO.findById(commentId);
    }

    public List<Comment> getCommentsByPost(int postId) throws Exception {
        return commentDAO.findByPostId(postId);
    }

    public List<Comment> getCommentsByAuthor(int authorId) throws Exception {
        return commentDAO.findByAuthorId(authorId);
    }

    public boolean updateComment(Comment comment) throws Exception {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        return commentDAO.update(comment);
    }

    public boolean deleteComment(int commentId) throws Exception {
        Optional<Comment> comment = commentDAO.findById(commentId);
        if (comment.isPresent()) {
            return commentDAO.delete(commentId);
        }
        return false;
    }

}
