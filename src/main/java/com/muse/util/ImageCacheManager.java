package com.muse.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muse.config.ImageCacheConfig;
import com.muse.models.ClothingCategory;
import com.muse.models.ImageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages local caching of clothing item images
 * Implements lazy loading strategy - images cached only when displayed
 */
public class ImageCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(ImageCacheManager.class);
    private static ImageCacheManager instance;
    private static final Object lock = new Object();

    private final CacheIndex cacheIndex;
    private final Gson gson;
    private final ExecutorService executorService;
    private long totalCacheSize = 0;

    private ImageCacheManager() {
        this.cacheIndex = new CacheIndex();
        this.gson = new Gson();
        this.executorService = Executors.newFixedThreadPool(ImageCacheConfig.THREAD_POOL_SIZE);
        initializeCache();
    }

    /**
     * Get singleton instance
     */
    public static ImageCacheManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ImageCacheManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize cache directories and load metadata index
     */
    public synchronized void initializeCache() {
        try {
            // Create cache directories
            Files.createDirectories(ImageCacheConfig.CACHE_CLOTHING_DIR);
            logger.info("Cache directories initialized at {}", ImageCacheConfig.CACHE_IMAGE_DIR);

            // Load metadata index from file
            loadMetadataIndex();

            // Clean up expired entries
            cleanupExpiredEntries();

            logger.info("Cache initialized. Index size: {}", cacheIndex.size());
        } catch (IOException e) {
            logger.error("Failed to initialize cache", e);
        }
    }

    /**
     * Load metadata index from metadata.json file
     */
    private void loadMetadataIndex() {
        Path metadataFile = ImageCacheConfig.CACHE_IMAGE_DIR.resolve(ImageCacheConfig.METADATA_INDEX_FILE);

        if (Files.exists(metadataFile)) {
            try {
                String content = new String(Files.readAllBytes(metadataFile));
                Map<String, ImageMetadata> metadataMap = gson.fromJson(content,
                        new TypeToken<Map<String, ImageMetadata>>(){}.getType());

                if (metadataMap != null) {
                    metadataMap.forEach((hash, metadata) -> {
                        cacheIndex.add(hash, metadata);
                        totalCacheSize += metadata.getFileSize();
                    });
                }
                logger.info("Loaded {} entries from metadata index", metadataMap != null ? metadataMap.size() : 0);
            } catch (Exception e) {
                logger.warn("Failed to load metadata index, rebuilding from disk", e);
                rebuildMetadataIndex();
            }
        }
    }

    /**
     * Save metadata index to file
     */
    private synchronized void saveMetadataIndex() {
        try {
            Path metadataFile = ImageCacheConfig.CACHE_IMAGE_DIR.resolve(ImageCacheConfig.METADATA_INDEX_FILE);
            Map<String, ImageMetadata> metadataMap = new HashMap<>();

            // Collect all metadata from cache index by iterating all entries
            cacheIndex.getAllMetadata().forEach(metadata -> {
                String hash = generateUrlHash(metadata.getOriginalUrl());
                metadataMap.put(hash, metadata);
            });

            String json = gson.toJson(metadataMap);
            Files.write(metadataFile, json.getBytes());
            logger.debug("Saved metadata index with {} entries", metadataMap.size());
        } catch (IOException e) {
            logger.error("Failed to save metadata index", e);
        }
    }

    /**
     * Rebuild metadata index from disk files
     */
    private void rebuildMetadataIndex() {
        try {
            Files.walk(ImageCacheConfig.CACHE_CLOTHING_DIR, 1)
                    .filter(f -> f.toString().endsWith(".meta.json"))
                    .forEach(metaFile -> {
                        try {
                            String content = new String(Files.readAllBytes(metaFile));
                            ImageMetadata metadata = gson.fromJson(content, ImageMetadata.class);
                            String hash = metaFile.getFileName().toString()
                                    .replace(ImageCacheConfig.METADATA_FILE_EXTENSION, "");
                            cacheIndex.add(hash, metadata);
                            totalCacheSize += metadata.getFileSize();
                        } catch (Exception e) {
                            logger.warn("Failed to load metadata from {}", metaFile, e);
                        }
                    });
            logger.info("Rebuilt metadata index from disk: {} entries", cacheIndex.size());
        } catch (IOException e) {
            logger.error("Failed to rebuild metadata index", e);
        }
    }

    /**
     * Get or cache an image
     * Primary method for lazy loading - downloads only if not cached
     */
    public String getOrCacheImage(int itemId, String imageUrl, ClothingCategory category) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        String urlHash = generateUrlHash(imageUrl);

        // Check if already cached
        Optional<ImageMetadata> cached = cacheIndex.findByUrlHash(urlHash);
        if (cached.isPresent()) {
            ImageMetadata metadata = cached.get();
            if (!metadata.isExpired()) {
                metadata.updateLastAccessedTime();
                Path cachedPath = ImageCacheConfig.CACHE_CLOTHING_DIR.resolve(metadata.getFileName());
                if (Files.exists(cachedPath)) {
                    String fileUrl = cachedPath.toUri().toString();
                    logger.debug("Cache hit for item {}: {}", itemId, fileUrl);
                    return fileUrl;
                }
            } else {
                logger.debug("Cache entry expired for {}", urlHash);
                cacheIndex.remove(urlHash);
            }
        }

        // Cache miss - download and cache
        return cacheImage(itemId, imageUrl, category);
    }

    /**
     * Download and cache an image
     */
    public String cacheImage(int itemId, String imageUrl, ClothingCategory category) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        String urlHash = generateUrlHash(imageUrl);
        String fileName = urlHash + ".jpg";
        Path cachedPath = ImageCacheConfig.CACHE_CLOTHING_DIR.resolve(fileName);

        try {
            // Check size before downloading
            long fileSize = getRemoteFileSize(imageUrl);
            if (fileSize > ImageCacheConfig.MAX_IMAGE_SIZE) {
                logger.warn("Image too large ({} bytes): {}", fileSize, imageUrl);
                return null;
            }

            // Check if cache is full, evict if needed
            if (totalCacheSize + fileSize > ImageCacheConfig.MAX_CACHE_SIZE) {
                evictLRUEntries(fileSize);
            }

            // Download image with retries
            byte[] imageData = downloadImageWithRetry(imageUrl);
            if (imageData == null) {
                logger.warn("Failed to download image after retries: {}", imageUrl);
                return null;
            }

            // Write to cache
            Files.write(cachedPath, imageData);
            totalCacheSize += imageData.length;

            // Create and save metadata
            String etag = getRemoteETag(imageUrl);
            ImageMetadata metadata = new ImageMetadata(itemId, category.name(), imageUrl, imageData.length, etag);
            metadata.setFileName(fileName);

            // Save metadata to separate file
            Path metaFile = ImageCacheConfig.CACHE_CLOTHING_DIR.resolve(fileName + ImageCacheConfig.METADATA_FILE_EXTENSION);
            Files.write(metaFile, gson.toJson(metadata).getBytes());

            // Add to index
            cacheIndex.add(urlHash, metadata);
            saveMetadataIndex();

            String fileUrl = cachedPath.toUri().toString();
            logger.info("Cached image for item {}: {} ({} bytes)", itemId, fileUrl, imageData.length);
            return fileUrl;

        } catch (Exception e) {
            logger.error("Failed to cache image {}", imageUrl, e);
            return null;
        }
    }

    /**
     * Cache image asynchronously (non-blocking)
     */
    public void cacheImageAsync(int itemId, String imageUrl, ClothingCategory category,
                                 Consumer<String> onSuccess, Consumer<Exception> onError) {
        executorService.submit(() -> {
            try {
                String cachedPath = getOrCacheImage(itemId, imageUrl, category);
                if (cachedPath != null) {
                    onSuccess.accept(cachedPath);
                } else {
                    onError.accept(new IOException("Failed to cache image"));
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    /**
     * Check if an image is cached and not expired
     */
    public boolean isCached(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        String urlHash = generateUrlHash(imageUrl);
        Optional<ImageMetadata> metadata = cacheIndex.findByUrlHash(urlHash);
        return metadata.isPresent() && !metadata.get().isExpired();
    }

    /**
     * Get cached image path
     */
    public Optional<String> getCachedImagePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return Optional.empty();
        }
        String urlHash = generateUrlHash(imageUrl);
        return cacheIndex.findByUrlHash(urlHash)
                .filter(m -> !m.isExpired())
                .map(m -> ImageCacheConfig.CACHE_CLOTHING_DIR.resolve(m.getFileName()).toUri().toString());
    }

    /**
     * Invalidate a specific cache entry
     */
    public synchronized void invalidateCache(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        String urlHash = generateUrlHash(imageUrl);
        Optional<ImageMetadata> metadata = cacheIndex.findByUrlHash(urlHash);

        if (metadata.isPresent()) {
            try {
                Path cachedPath = ImageCacheConfig.CACHE_CLOTHING_DIR.resolve(metadata.get().getFileName());
                Path metaPath = Paths.get(cachedPath + ImageCacheConfig.METADATA_FILE_EXTENSION);

                Files.deleteIfExists(cachedPath);
                Files.deleteIfExists(metaPath);

                totalCacheSize -= metadata.get().getFileSize();
                cacheIndex.remove(urlHash);
                saveMetadataIndex();

                logger.info("Invalidated cache for: {}", imageUrl);
            } catch (IOException e) {
                logger.error("Failed to invalidate cache", e);
            }
        }
    }

    /**
     * Clear entire cache
     */
    public synchronized void clearCache() {
        try {
            Files.walk(ImageCacheConfig.CACHE_CLOTHING_DIR, 1)
                    .filter(f -> !Files.isDirectory(f))
                    .forEach(f -> {
                        try {
                            Files.delete(f);
                        } catch (IOException e) {
                            logger.warn("Failed to delete cache file: {}", f);
                        }
                    });

            cacheIndex.clear();
            totalCacheSize = 0;
            logger.info("Cache cleared completely");
        } catch (IOException e) {
            logger.error("Failed to clear cache", e);
        }
    }

    /**
     * Get total cache size in bytes
     */
    public long getCacheSize() {
        return totalCacheSize;
    }

    /**
     * Get cache statistics
     */
    public CacheIndex.CacheStatistics getCacheStatistics() {
        return cacheIndex.getStatistics();
    }

    /**
     * Cleanup expired entries
     */
    private synchronized void cleanupExpiredEntries() {
        try {
            cacheIndex.removeExpired();
            // Also delete files for expired entries
            Files.walk(ImageCacheConfig.CACHE_CLOTHING_DIR, 1)
                    .filter(f -> !Files.isDirectory(f))
                    .filter(f -> !f.getFileName().toString().equals(ImageCacheConfig.METADATA_INDEX_FILE))
                    .forEach(f -> {
                        String fileName = f.getFileName().toString();
                        String hash = fileName.endsWith(ImageCacheConfig.METADATA_FILE_EXTENSION) ?
                                fileName.replace(ImageCacheConfig.METADATA_FILE_EXTENSION, "") :
                                fileName.replace(".jpg", "");

                        if (cacheIndex.findByUrlHash(hash).isEmpty()) {
                            try {
                                Files.deleteIfExists(f);
                            } catch (IOException e) {
                                logger.warn("Failed to delete expired file: {}", f);
                            }
                        }
                    });
            saveMetadataIndex();
        } catch (IOException e) {
            logger.error("Failed to cleanup expired entries", e);
        }
    }

    /**
     * Evict least recently used entries to make space
     */
    private synchronized void evictLRUEntries(long spaceNeeded) {
        logger.info("Cache full, evicting LRU entries. Need {} bytes", spaceNeeded);

        // Get all entries and sort by last accessed time (oldest first)
        List<ImageMetadata> allEntries = cacheIndex.getAllMetadata().stream()
                .sorted((a, b) -> Long.compare(a.getLastAccessedAt(), b.getLastAccessedAt()))
                .collect(Collectors.toList());

        long freedSpace = 0;
        for (ImageMetadata metadata : allEntries) {
            if (freedSpace >= spaceNeeded) {
                break;
            }

            try {
                String urlHash = generateUrlHash(metadata.getOriginalUrl());
                Path cachedPath = ImageCacheConfig.CACHE_CLOTHING_DIR.resolve(metadata.getFileName());
                Path metaPath = Paths.get(cachedPath + ImageCacheConfig.METADATA_FILE_EXTENSION);

                Files.deleteIfExists(cachedPath);
                Files.deleteIfExists(metaPath);

                freedSpace += metadata.getFileSize();
                totalCacheSize -= metadata.getFileSize();
                cacheIndex.remove(urlHash);

                logger.debug("Evicted cache entry: {} ({} bytes)", metadata.getFileName(), metadata.getFileSize());
            } catch (IOException e) {
                logger.warn("Failed to evict cache entry: {}", metadata.getFileName(), e);
            }
        }

        logger.info("Evicted {} bytes of cache", freedSpace);
    }

    /**
     * Generate SHA-256 hash of URL
     */
    private String generateUrlHash(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Failed to generate URL hash", e);
            return String.valueOf(url.hashCode());
        }
    }

    /**
     * Download image with retry logic
     */
    private byte[] downloadImageWithRetry(String urlString) {
        for (int attempt = 0; attempt < ImageCacheConfig.MAX_RETRIES; attempt++) {
            try {
                return downloadImage(urlString);
            } catch (Exception e) {
                logger.warn("Download attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt < ImageCacheConfig.MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(ImageCacheConfig.RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Download image from URL
     */
    private byte[] downloadImage(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(ImageCacheConfig.DOWNLOAD_TIMEOUT_MS);
        conn.setReadTimeout(ImageCacheConfig.DOWNLOAD_TIMEOUT_MS);

        try (InputStream is = conn.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * Get remote file size
     */
    private long getRemoteFileSize(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(ImageCacheConfig.DOWNLOAD_TIMEOUT_MS);
            return conn.getContentLengthLong();
        } catch (Exception e) {
            logger.debug("Failed to get remote file size: {}", e.getMessage());
            return Long.MAX_VALUE;
        }
    }

    /**
     * Get ETag header for change detection
     */
    private String getRemoteETag(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(ImageCacheConfig.DOWNLOAD_TIMEOUT_MS);
            return conn.getHeaderField("ETag");
        } catch (Exception e) {
            logger.debug("Failed to get ETag: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Shutdown cache manager and cleanup resources
     */
    public void shutdown() {
        saveMetadataIndex();
        executorService.shutdown();
        logger.info("ImageCacheManager shutdown");
    }
}
