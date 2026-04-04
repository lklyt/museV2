package com.muse.service;

import com.muse.dao.PostDAO;
import com.muse.dao.PostDAOImpl;
import com.muse.models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostDAO postDAO = new PostDAOImpl();

    public Post createPost(int authorId, int communityId, String title, String content) throws Exception {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        Post post = new Post(authorId, communityId, title, content);
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

    public boolean updatePost(Post post) throws Exception {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        return postDAO.update(post);
    }

    public boolean deletePost(int postId) throws Exception {
        return postDAO.delete(postId);
    }

    public boolean likePost(int postId) throws Exception {
        return postDAO.incrementLikes(postId);
    }

    public boolean unlikePost(int postId) throws Exception {
        return postDAO.decrementLikes(postId);
    }

    public int getLikesCount(int postId) throws Exception {
        Optional<Post> post = postDAO.findById(postId);
        return post.map(Post::getLikesCount).orElse(0);
    }
}
