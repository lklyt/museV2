package com.muse.dao;

import com.muse.models.Community;
import java.util.Optional;
import java.util.List;

public interface CommunityDAO {
    Community save(Community community) throws Exception;
    Optional<Community> findById(int communityId) throws Exception;
    Optional<Community> findByName(String name) throws Exception;
    List<Community> findAll() throws Exception;
    boolean update(Community community) throws Exception;
    boolean delete(int communityId) throws Exception;
}
