package com.muse.util;

import com.muse.models.ImageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory index for fast cache lookups
 * Supports O(1) lookups by URL hash, itemId, and category
 */
public class CacheIndex {
    private static final Logger logger = LoggerFactory.getLogger(CacheIndex.class);

    // URL hash -> ImageMetadata
    private final Map<String, ImageMetadata> imageIndex = new ConcurrentHashMap<>();

    // itemId -> List of URL hashes
    private final Map<Integer, List<String>> itemIndex = new ConcurrentHashMap<>();

    // category -> List of URL hashes
    private final Map<String, List<String>> categoryIndex = new ConcurrentHashMap<>();

    private long cacheHits = 0;
    private long cacheMisses = 0;

    /**
     * Add or update metadata in the index
     */
    public void add(String urlHash, ImageMetadata metadata) {
        imageIndex.put(urlHash, metadata);

        // Add to item index
        int itemId = metadata.getItemId();
        itemIndex.computeIfAbsent(itemId, k -> Collections.synchronizedList(new ArrayList<>()))
                 .add(urlHash);

        // Add to category index
        String category = metadata.getCategory();
        categoryIndex.computeIfAbsent(category, k -> Collections.synchronizedList(new ArrayList<>()))
                     .add(urlHash);

        logger.debug("Added to index: {} (itemId: {}, category: {})", urlHash, itemId, category);
    }

    /**
     * Find metadata by URL hash
     */
    public Optional<ImageMetadata> findByUrlHash(String urlHash) {
        ImageMetadata metadata = imageIndex.get(urlHash);
        if (metadata != null) {
            cacheHits++;
            return Optional.of(metadata);
        }
        cacheMisses++;
        return Optional.empty();
    }

    /**
     * Find all metadata for an item
     */
    public List<ImageMetadata> findByItemId(int itemId) {
        List<String> urlHashes = itemIndex.getOrDefault(itemId, new ArrayList<>());
        return urlHashes.stream()
                        .map(imageIndex::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    /**
     * Find all metadata for a category
     */
    public List<ImageMetadata> findByCategory(String category) {
        List<String> urlHashes = categoryIndex.getOrDefault(category, new ArrayList<>());
        return urlHashes.stream()
                        .map(imageIndex::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    /**
     * Remove metadata from index
     */
    public void remove(String urlHash) {
        ImageMetadata metadata = imageIndex.remove(urlHash);
        if (metadata != null) {
            // Remove from item index
            List<String> itemHashes = itemIndex.get(metadata.getItemId());
            if (itemHashes != null) {
                itemHashes.remove(urlHash);
            }

            // Remove from category index
            List<String> categoryHashes = categoryIndex.get(metadata.getCategory());
            if (categoryHashes != null) {
                categoryHashes.remove(urlHash);
            }

            logger.debug("Removed from index: {}", urlHash);
        }
    }

    /**
     * Remove all expired entries
     */
    public void removeExpired() {
        List<String> expiredHashes = imageIndex.entrySet().stream()
                                                .filter(e -> e.getValue().isExpired())
                                                .map(Map.Entry::getKey)
                                                .collect(Collectors.toList());

        expiredHashes.forEach(this::remove);
        logger.info("Removed {} expired entries from index", expiredHashes.size());
    }

    /**
     * Clear entire index
     */
    public void clear() {
        imageIndex.clear();
        itemIndex.clear();
        categoryIndex.clear();
        cacheHits = 0;
        cacheMisses = 0;
        logger.info("Cache index cleared");
    }

    /**
     * Get all metadata entries
     */
    public List<ImageMetadata> getAllMetadata() {
        return new ArrayList<>(imageIndex.values());
    }

    /**
     * Get total number of entries in index
     */
    public int size() {
        return imageIndex.size();
    }

    /**
     * Get cache hit rate
     */
    public double getHitRate() {
        long total = cacheHits + cacheMisses;
        if (total == 0) return 0.0;
        return (double) cacheHits / total * 100;
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(cacheHits, cacheMisses, getHitRate());
    }

    /**
     * Simple statistics class
     */
    public static class CacheStatistics {
        public final long hits;
        public final long misses;
        public final double hitRate;

        public CacheStatistics(long hits, long misses, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
        }

        @Override
        public String toString() {
            return String.format("Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.2f%%", hits, misses, hitRate);
        }
    }
}
