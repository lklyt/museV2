package com.muse.models;

import java.util.ArrayList;

/**
 * Implemented by any model class that should appear in search results.
 *
 * Index – the object's primary identifier. This is the text that
 * will be shown as the autocomplete suggestion and carries the highest search
 * weight.
 */

public interface Searchable {

    /**
     * Returns the ordered list of searchable keywords for this object.
     *
     * The first element (index 0) must be the primary identifier and will
     * receive the highest relevance weight in the scoring algorithm.
     * All subsequent elements are treated as supporting keywords.
     *
     * The search engine will treat it as an empty list and the object
     * will not appear in results if the search result does not exists.
     *
     */
    ArrayList<String> getSearchKeywords();

    /**
     * Tells the search engine what category this object belongs to,
     * so it can be routed to the correct result bucket.
     */
    SearchType getSearchType();
}
