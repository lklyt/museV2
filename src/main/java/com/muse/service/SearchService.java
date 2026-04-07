package com.muse.service;

import com.muse.dao.UserDAO;
import com.muse.dao.CommunityDAO;
import com.muse.dao.PostDAO;
import com.muse.models.Searchable;
import com.muse.models.SearchResult;
import com.muse.models.SearchType;
import com.muse.models.User;
import com.muse.models.Community;
import com.muse.models.Post;
import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Core search engine for MUSE.
 *
 * Every Searchable object exposes a keyword list via
 * getSearchKeywords(). The first keyword (index 0) is treated as
 * the primary identifier (username, post title, etc.) and receives the highest
 * weight. All remaining keywords are treated as supporting tags.
 *
 * Score breakdown per keyword match:
 *   Primary keyword (index 0):
 *     exact match   → +100
 *     starts with   → +75
 *     contains      → +50
 *     word partial  → +8
 *
 *   Supporting keywords (index 1+):
 *     exact match   → +30
 *     starts with   → +15
 *     contains      → +10
 *     word partial  → +3
 *
 * Objects with a total score of 0 are excluded from results.
 * Results are returned sorted highest-score-first.
 *
 */
public class SearchService {

    private UserDAO userDAO;
    private CommunityDAO communityDAO;
    private PostDAO postDAO;

    // ClothingItem is kept as an in-memory list for efficiency.

    private ArrayList<ClothingItem> clothingItems;

    public SearchService(UserDAO userDAO, CommunityDAO communityDAO, PostDAO postDAO) {
        this.userDAO = userDAO;
        this.communityDAO = communityDAO;
        this.postDAO = postDAO;
        this.clothingItems = new ArrayList<ClothingItem>();
    }

    // Temporary setter until ClothingItemDAO exists
    public void setClothingItems(ArrayList<ClothingItem> clothingItems) {
        if (clothingItems != null) {
            this.clothingItems = clothingItems;
        } else {
            this.clothingItems = new ArrayList<ClothingItem>();
        }
    }

    // Default search type is POSTS
    public SearchResult searchAll(String query) {
        return searchAll(query, SearchType.POSTS);
    }

    public SearchResult searchAll(String query, SearchType selectedType) {
        SearchResult result = new SearchResult();

        if (selectedType == null) {
            selectedType = SearchType.POSTS;
        }

        try {
            if (selectedType == SearchType.PEOPLE) {
                ArrayList<User> allUsers = new ArrayList<User>(userDAO.findAll());
                ArrayList<User> matched = getSortedMatches(allUsers, query);
                for (int i = 0; i < matched.size(); i++) {
                    result.addUser(matched.get(i));
                }

            } else if (selectedType == SearchType.COMMUNITIES) {
                ArrayList<Community> allCommunities = new ArrayList<Community>(communityDAO.findAll());
                ArrayList<Community> matched = getSortedMatches(allCommunities, query);
                for (int i = 0; i < matched.size(); i++) {
                    result.addCommunity(matched.get(i));
                }

            } else if (selectedType == SearchType.CLOTHING_ITEMS) {
                ArrayList<ClothingItem> matched = getSortedMatches(clothingItems, query);
                for (int i = 0; i < matched.size(); i++) {
                    result.addClothingItem(matched.get(i));
                }

            } else { // POSTS (default)
                ArrayList<Post> allPosts = new ArrayList<Post>(postDAO.findAll());
                ArrayList<Post> matched = getSortedMatches(allPosts, query);
                for (int i = 0; i < matched.size(); i++) {
                    result.addPost(matched.get(i));
                }
            }

        } catch (Exception e) {
            // Return whatever partial result was built before the exception
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Filters and ranks clothing items by category and query.
     * Used by the half-screen clothing picker on the avatar page.
     */
    public ArrayList<ClothingItem> filterClothing(String query, ClothingCategory category) {
        ArrayList<ClothingItem> categoryMatches = new ArrayList<ClothingItem>();

        for (int i = 0; i < clothingItems.size(); i++) {
            ClothingItem item = clothingItems.get(i);
            if (category == null || item.getCategory() == category) {
                categoryMatches.add(item);
            }
        }

        return getSortedMatches(categoryMatches, query);
    }

    /**
     * Returns up to 5 autocomplete suggestions for the given query and type.
     * Suggestions are the primary keyword (index 0) of matching objects,
     * sorted by relevance score. Duplicates are skipped.
     */
    public ArrayList<String> getSuggestions(String query, SearchType selectedType) {
        ArrayList<String> suggestions = new ArrayList<String>();

        if (isBlank(query)) {
            return suggestions;
        }

        if (selectedType == null) {
            selectedType = SearchType.POSTS;
        }

        ArrayList<SearchMatch<String>> scored = new ArrayList<SearchMatch<String>>();

        try {
            if (selectedType == SearchType.PEOPLE) {
                ArrayList<User> allUsers = new ArrayList<User>(userDAO.findAll());
                collectSuggestions(allUsers, query, scored);

            } else if (selectedType == SearchType.COMMUNITIES) {
                ArrayList<Community> allCommunities = new ArrayList<Community>(communityDAO.findAll());
                collectSuggestions(allCommunities, query, scored);

            } else if (selectedType == SearchType.CLOTHING_ITEMS) {
                collectSuggestions(clothingItems, query, scored);

            } else {
                ArrayList<Post> allPosts = new ArrayList<Post>(postDAO.findAll());
                collectSuggestions(allPosts, query, scored);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(scored, new Comparator<SearchMatch<String>>() {
            public int compare(SearchMatch<String> a, SearchMatch<String> b) {
                return b.getScore() - a.getScore();
            }
        });

        int limit = Math.min(scored.size(), 5);
        for (int i = 0; i < limit; i++) {
            suggestions.add(scored.get(i).getItem());
        }

        return suggestions;
    }

    /**
     * Returns a new list containing only the items that matched the query,
     * sorted by descending relevance score.
     * If the query is blank, the original list is returned as-is (no filtering).
     */
    private <T extends Searchable> ArrayList<T> getSortedMatches(ArrayList<T> items, String query) {
        ArrayList<T> result = new ArrayList<T>();

        if (items == null) {
            return result;
        }

        // Blank query → return everything (used by filterClothing when search bar is empty)
        if (isBlank(query)) {
            result.addAll(items);
            return result;
        }

        ArrayList<SearchMatch<T>> scored = new ArrayList<SearchMatch<T>>();

        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            int score = calculateScore(item, query);
            if (score > 0) {
                scored.add(new SearchMatch<T>(item, score));
            }
        }

        Collections.sort(scored, new Comparator<SearchMatch<T>>() {
            public int compare(SearchMatch<T> a, SearchMatch<T> b) {
                return b.getScore() - a.getScore();
            }
        });

        for (int i = 0; i < scored.size(); i++) {
            result.add(scored.get(i).getItem());
        }

        return result;
    }

    /**
     * Adds suggestion entries (primary keyword of each matching item) into the
     * shared scored list. Skips duplicates (case-insensitive).
     */
    private void collectSuggestions(
            ArrayList<? extends Searchable> items,
            String query,
            ArrayList<SearchMatch<String>> scored) {

        for (int i = 0; i < items.size(); i++) {
            Searchable item = items.get(i);
            int score = calculateScore(item, query);

            if (score > 0) {
                ArrayList<String> keywords = item.getSearchKeywords();
                // The primary keyword (index 0) is the suggestion text
                if (keywords != null && keywords.size() > 0 && keywords.get(0) != null) {
                    String suggestionText = keywords.get(0);
                    if (!containsSuggestion(scored, suggestionText)) {
                        scored.add(new SearchMatch<String>(suggestionText, score));
                    }
                }
            }
        }
    }

    private boolean containsSuggestion(ArrayList<SearchMatch<String>> suggestions, String text) {
        for (int i = 0; i < suggestions.size(); i++) {
            if (suggestions.get(i).getItem().equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Index 0 of the keyword list is the primary identifier and carries the
     * highest weight. All subsequent keywords are treated as supporting tags
     * with lower weights. A score of 0 means no match at all.
     */
    private int calculateScore(Searchable item, String query) {
        if (item == null || isBlank(query)) {
            return 0;
        }

        ArrayList<String> keywords = item.getSearchKeywords();
        if (keywords == null || keywords.size() == 0) {
            return 0;
        }

        int score = 0;
        String normalizedQuery = normalize(query);
        String[] queryParts = normalizedQuery.split("\\s+");

        for (int i = 0; i < keywords.size(); i++) {
            String keyword = normalize(keywords.get(i));

            if (keyword.length() == 0) {
                continue;
            }

            boolean isPrimary = (i == 0);

            // Full keyword match scoring
            if (keyword.equals(normalizedQuery)) {
                score += isPrimary ? 100 : 30;
            } else if (keyword.startsWith(normalizedQuery)) {
                score += isPrimary ? 75 : 15;
            } else if (keyword.contains(normalizedQuery)) {
                score += isPrimary ? 50 : 10;
            }

            // Partial word scoring (handles multi-word queries like "dark academia")
            for (int p = 0; p < queryParts.length; p++) {
                String part = queryParts[p];
                if (part.length() == 0) {
                    continue;
                }
                if (keyword.contains(part)) {
                    score += isPrimary ? 8 : 3;
                }
            }
        }

        return score;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    private static class SearchMatch<T> {
        private final T item;
        private final int score;

        public SearchMatch(T item, int score) {
            this.item = item;
            this.score = score;
        }

        public T getItem() {
            return item;
        }

        public int getScore() {
            return score;
        }
    }
}
