package com.muse.dao;

import com.muse.models.Community;
import java.util.Optional;
import java.util.List;

public interface CommunityDAO {
    Community save(Community community) throws Exception;
    Optional<Community> findById(int communityId) throws Exception;
    Optional<Community> findByName(String name) throws Exception;
    List<Community> findAll() throws Exception;
    List<Community> findByMemberId(int userId) throws Exception;
    boolean update(Community community) throws Exception;
    boolean delete(int communityId) throws Exception;
    boolean addMember(int communityId, int userId) throws Exception;
    boolean removeMember(int communityId, int userId) throws Exception;
    boolean isMember(int communityId, int userId) throws Exception;
    int getMemberCount(int communityId) throws Exception;
}
