package com.muse.service;

import com.muse.models.User;
import com.muse.dao.UserDAO;
import com.muse.dao.UserDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO = new UserDAOImpl();

    public Optional<User> register(String username, String email, String password, String displayName) throws Exception {
        // Validate inputs
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        String passwordHash = hashPassword(password);
        User user = new User(username, email, passwordHash);
        return Optional.of(userDAO.save(user));
    }

    public Optional<User> login(String username, String password) throws Exception {
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (verifyPassword(password, user.getPasswordHash())) {
                logger.info("User logged in: " + username);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(int userId) throws Exception {
        return userDAO.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) throws Exception {
        return userDAO.findByUsername(username);
    }

    public boolean updateUserProfile(User user) throws Exception {
        return userDAO.update(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (verifyPassword(oldPassword, user.getPasswordHash())) {
                user.setPasswordHash(hashPassword(newPassword));
                return userDAO.update(user);
            }
        }
        return false;
    }

    private String hashPassword(String password) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());

        byte[] saltAndHash = new byte[salt.length + hashedPassword.length];
        System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
        System.arraycopy(hashedPassword, 0, saltAndHash, salt.length, hashedPassword.length);

        return Base64.getEncoder().encodeToString(saltAndHash);
    }

    private boolean verifyPassword(String password, String hash) throws Exception {
        byte[] saltAndHash = Base64.getDecoder().decode(hash);
        byte[] salt = new byte[16];
        System.arraycopy(saltAndHash, 0, salt, 0, salt.length);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());

        for (int i = 0; i < hashedPassword.length; i++) {
            if (saltAndHash[salt.length + i] != hashedPassword[i]) {
                return false;
            }
        }
        return true;
    }
}
