package com.muse.dao;

import com.muse.config.DatabaseConfig;
import com.muse.models.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentDAOImpl implements CommentDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommentDAOImpl.class);

    @Override
    public Comment save(Comment comment) throws Exception {
        String sql = "INSERT INTO comments (post_id, author_id, content) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getPostId());
            stmt.setInt(2, comment.getAuthorId());
            stmt.setString(3, comment.getContent());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating comment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setCommentId(generatedKeys.getInt(1));
                    comment.setCreatedAt(LocalDateTime.now());
                    comment.setUpdatedAt(LocalDateTime.now());
                    logger.info("Comment created with ID: " + comment.getCommentId());
                }
            }
        }
        return comment;
    }

    @Override
    public Optional<Comment> findById(int commentId) throws Exception {
        String sql = "SELECT c.*, u.username FROM comments c " +
                     "JOIN users u ON c.author_id = u.user_id WHERE c.comment_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToComment(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Comment> findByPostId(int postId) throws Exception {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c " +
                     "JOIN users u ON c.author_id = u.user_id WHERE c.post_id = ? " +
                     "ORDER BY c.created_at ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    @Override
    public List<Comment> findByAuthorId(int authorId) throws Exception {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c " +
                     "JOIN users u ON c.author_id = u.user_id WHERE c.author_id = ? " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        }
        return comments;
    }

    @Override
    public boolean update(Comment comment) throws Exception {
        String sql = "UPDATE comments SET content = ? WHERE comment_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getCommentId());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int commentId) throws Exception {
        String sql = "DELETE FROM comments WHERE comment_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            return stmt.executeUpdate() > 0;
        }
    }

    //convert from database to Comment
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setCommentId(rs.getInt("comment_id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setAuthorId(rs.getInt("author_id"));
        comment.setAuthorUsername(rs.getString("username"));
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        comment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return comment;
    }
}
