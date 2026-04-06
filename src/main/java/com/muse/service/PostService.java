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
}
