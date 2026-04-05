package com.muse.dao;

import com.muse.models.User;
import java.util.Optional;
import java.util.List;

public interface UserDAO {
    User save(User user) throws Exception;
    Optional<User> findById(int userId) throws Exception;
    Optional<User> findByUsername(String username) throws Exception;
    Optional<User> findByEmail(String email) throws Exception;
    List<User> findAll() throws Exception;
    boolean update(User user) throws Exception;
    boolean delete(int userId) throws Exception;
    boolean usernameExists(String username) throws Exception;
    boolean emailExists(String email) throws Exception;

    // Follow functionality
    boolean followUser(int followerId, int followingId) throws Exception;
    boolean unfollowUser(int followerId, int followingId) throws Exception;
    List<User> getFollowers(int userId) throws Exception;
    List<User> getFollowing(int userId) throws Exception;
    boolean isFollowing(int followerId, int followingId) throws Exception;
    int getFollowerCount(int userId) throws Exception;
    int getFollowingCount(int userId) throws Exception;
}
