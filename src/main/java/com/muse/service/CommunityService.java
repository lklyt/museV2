package com.muse.service;

import com.muse.dao.CommunityDAO;
import com.muse.dao.CommunityDAOImpl;
import com.muse.models.Community;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CommunityService {
    private static final Logger logger = LoggerFactory.getLogger(CommunityService.class);
    private final CommunityDAO communityDAO = new CommunityDAOImpl();

    public Community createCommunity(String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Community name cannot be empty");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new IllegalArgumentException("Community name must be between 3 and 100 characters");
        }

        if (communityDAO.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Community name already exists");
        }

        Community community = new Community(name);
        return communityDAO.save(community);
    }

    public Optional<Community> getCommunityById(int communityId) throws Exception {
        return communityDAO.findById(communityId);
    }

    public Optional<Community> getCommunityByName(String name) throws Exception {
        return communityDAO.findByName(name);
    }

    public List<Community> getAllCommunities() throws Exception {
        return communityDAO.findAll();
    }

    public boolean updateCommunity(Community community) throws Exception {
        if (community.getName() == null || community.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Community name cannot be empty");
        }
        return communityDAO.update(community);
    }

    public boolean deleteCommunity(int communityId) throws Exception {
        return communityDAO.delete(communityId);
    }
}
