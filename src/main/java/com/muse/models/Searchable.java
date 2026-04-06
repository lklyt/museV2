package com.muse.models;

import java.util.ArrayList;

/**
 * Implemented by any model class that should appear in search results.
 *
 * <p><b>Keyword convention (important – teammates must follow this):</b>
 * <ul>
 *   <li><b>Index 0</b> – the object's primary identifier. This is the text that
 *       will be shown as the autocomplete suggestion and carries the highest search
 *       weight. Examples: a {@code User}'s username, a {@code Post}'s title, a
 *       {@code Community}'s name, a {@code ClothingItem}'s item name.</li>
 *   <li><b>Index 1+</b> – any secondary / supporting text and tags. Examples:
 *       a {@code User}'s display name, a {@code Post}'s description, a
 *       {@code Community}'s short description, aesthetic tags, color tags,
 *       category names, etc.</li>
 * </ul>
 *
 * <p><b>MySQL note:</b> You do <em>not</em> need a separate keywords table.
 * Just build the list from the fields that your DAO already loaded from the DB.
 * For example:
 * <pre>
 * // Inside User.java
 * {@literal @}Override
 * public ArrayList{@literal <}String{@literal >} getSearchKeywords() {
 *     ArrayList{@literal <}String{@literal >} keywords = new ArrayList{@literal <}{@literal >}();
 *     keywords.add(username);        // index 0 – primary identifier
 *     keywords.add(displayName);     // index 1 – secondary
 *     keywords.add(bio);             // index 2 – supporting text
 *     return keywords;
 * }
 * </pre>
 * If you later want database-side full-text search, add a {@code FULLTEXT} index
 * in MySQL on the relevant columns and let the DAO pre-filter before passing
 * the list to {@link com.muse.service.SearchService}.
 */
public interface Searchable {

    /**
     * Returns the ordered list of searchable keywords for this object.
     *
     * <p>The first element (index 0) must be the primary identifier and will
     * receive the highest relevance weight in the scoring algorithm.
     * All subsequent elements are treated as supporting keywords.
     *
     * <p>Returning {@code null} is allowed; the search engine will treat it
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
