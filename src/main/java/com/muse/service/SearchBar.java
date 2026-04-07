package com.muse.service;

import com.muse.models.SearchType;
import com.muse.models.SearchResult;
import com.muse.models.ClothingItem;
import com.muse.models.ClothingCategory;
import java.util.ArrayList;

public class SearchBar {

    private SearchService searchEngine;
    private SearchType selectedType;
    private ArrayList<String> recentSearches;

    private static final int MAX_RECENT_SEARCHES = 10;

    public SearchBar(SearchService searchEngine) {
        this.searchEngine = searchEngine;
        this.selectedType = SearchType.POSTS;
        this.recentSearches = new ArrayList<String>();
    }

    // Called by the filter buttons (Posts / People / Clothing Items / Communities)
    public void setSelectedType(SearchType type) {
        if (type != null) {
            this.selectedType = type;
        } else {
            this.selectedType = SearchType.POSTS;
        }
    }

    public SearchType getSelectedType() {
        return selectedType;
    }

    // Called by the main full-screen search bar.
    // Uses whichever filter button is currently selected (default: POSTS).
    public SearchResult globalSearch(String input) {
        addToRecentSearches(input);
        return searchEngine.searchAll(input, selectedType);
    }

    // Called by the clothing picker panel on the avatar/dress-up page.
    // Always searches clothing items only and covers half the screen,
    // leaving the other half for the avatar.
    // Pass null for category to search across all clothing categories.
    public ArrayList<ClothingItem> createStyleSearch(String input, ClothingCategory category) {
        return searchEngine.filterClothing(input, category);
    }

    // Called on every keystroke in the search bar to populate the suggestion
    // dropdown.
    // Returns up to 5 suggestions based on the currently selected type.
    // If the query is blank, returns recent searches instead.
    public ArrayList<String> getSuggestions(String input) {
        if (input == null || input.trim().length() == 0) {
            return getRecentSearches();
        }
        return searchEngine.getSuggestions(input, selectedType);
    }

    public ArrayList<String> getRecentSearches() {
        return new ArrayList<String>(recentSearches);
    }

    public void clearRecentSearches() {
        recentSearches.clear();
    }

    public void removeRecentSearch(String search) {
        for (int i = 0; i < recentSearches.size(); i++) {
            if (recentSearches.get(i).equalsIgnoreCase(search)) {
                recentSearches.remove(i);
                return;
            }
        }
    }

    // Adds a confirmed search (i.e. user pressed Enter or tapped a suggestion)
    // to the front of the recent list. Duplicates are moved to the front rather
    // than added again. List is capped at MAX_RECENT_SEARCHES.
    private void addToRecentSearches(String input) {
        if (input == null || input.trim().length() == 0) {
            return;
        }

        String trimmed = input.trim();

        for (int i = 0; i < recentSearches.size(); i++) {
            if (recentSearches.get(i).equalsIgnoreCase(trimmed)) {
                recentSearches.remove(i);
                break;
            }
        }

        recentSearches.add(0, trimmed);

        if (recentSearches.size() > MAX_RECENT_SEARCHES) {
            recentSearches.remove(recentSearches.size() - 1);
        }
    }
}