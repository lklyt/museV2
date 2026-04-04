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

    public Community createCommunity(String name, String description, int creatorId) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Community name cannot be empty");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new IllegalArgumentException("Community name must be between 3 and 100 characters");
        }

        if (communityDAO.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Community name already exists");
        }

        Community community = new Community(name, description, creatorId);
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

    public List<Community> getUserCommunities(int userId) throws Exception {
        return communityDAO.findByMemberId(userId);
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

    public boolean joinCommunity(int communityId, int userId) throws Exception {
        boolean isMember = communityDAO.isMember(communityId, userId);
        if (isMember) {
            throw new IllegalArgumentException("User is already a member");
        }
        return communityDAO.addMember(communityId, userId);
    }

    public boolean leaveCommunity(int communityId, int userId) throws Exception {
        return communityDAO.removeMember(communityId, userId);
    }

    public boolean isMember(int communityId, int userId) throws Exception {
        return communityDAO.isMember(communityId, userId);
    }

    public int getMemberCount(int communityId) throws Exception {
        return communityDAO.getMemberCount(communityId);
    }
}
