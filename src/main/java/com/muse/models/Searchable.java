package com.muse.models;

import java.util.ArrayList;

/**
 * Implemented by any model class that should appear in search results.
 *
 * Index – the object's primary identifier. This is the text that
 * will be shown as the autocomplete suggestion and carries the highest search
 * weight. Examples: a {@code User}'s username, a {@code Post}'s title, a
 * {@code Community}'s name, a {@code ClothingItem}'s item name.
 * Index 1+ – any secondary / supporting text and tags. Examples:
 * a {@code User}'s display name, a {@code Post}'s description, a
 * {@code Community}'s short description, aesthetic tags, color tags,
 * category names, etc.
 */

public interface Searchable {

    /**
     * Returns the ordered list of searchable keywords for this object.
     *
     * The first element (index 0) must be the primary identifier and will
     * receive the highest relevance weight in the scoring algorithm.
     * All subsequent elements are treated as supporting keywords.
     *
     * Returning {@code null} is allowed; the search engine will treat it
     * as an empty list and the object will not appear in results.
     *
     * @return ordered keyword list, never modified by the search engine
     */
    ArrayList<String> getSearchKeywords();

    /**
     * Tells the search engine what category this object belongs to,
     * so it can be routed to the correct result bucket.
     *
     * @return the {@link SearchType} of this object
     */
    SearchType getSearchType();
}
