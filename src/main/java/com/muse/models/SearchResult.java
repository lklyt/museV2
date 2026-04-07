package com.muse.models;

import java.util.ArrayList;

public class SearchResult {

    private ArrayList<User> users;
    private ArrayList<Community> communities;
    private ArrayList<Post> posts;
    private ArrayList<ClothingItem> clothingItems;

    public SearchResult() {
        users = new ArrayList<User>();
        communities = new ArrayList<Community>();
        posts = new ArrayList<Post>();
        clothingItems = new ArrayList<ClothingItem>();
    }

    public void addUser(User user) {
        if (user != null) {
            users.add(user);
        }
    }

    public void addCommunity(Community community) {
        if (community != null) {
            communities.add(community);
        }
    }

    public void addPost(Post post) {
        if (post != null) {
            posts.add(post);
        }
    }

    public void addClothingItem(ClothingItem item) {
        if (item != null) {
            clothingItems.add(item);
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<Community> getCommunities() {
        return communities;
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public ArrayList<ClothingItem> getClothingItems() {
        return clothingItems;
    }

    public boolean isEmpty() {
        return users.isEmpty() &&
                communities.isEmpty() &&
                posts.isEmpty() &&
                clothingItems.isEmpty();
    }

    public int getTotalResultCount() {
        return users.size() + communities.size() + posts.size() + clothingItems.size();
    }

    public void clear() {
        users.clear();
        communities.clear();
        posts.clear();
        clothingItems.clear();
    }
}