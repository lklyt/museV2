package com.muse.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageCacheConfig {
    // Cache directories
    public static final Path CACHE_BASE_DIR = Paths.get(
        System.getProperty("user.home"), ".muse", "cache"
    );
    public static final Path CACHE_IMAGE_DIR = CACHE_BASE_DIR.resolve("images");
    public static final Path CACHE_CLOTHING_DIR = CACHE_IMAGE_DIR.resolve("clothing");
    public static final String METADATA_INDEX_FILE = "metadata.json";

    // Size limits
    public static final long MAX_CACHE_SIZE = 500L * 1024 * 1024;  // 500MB
    public static final long MAX_IMAGE_SIZE = 50L * 1024 * 1024;   // 50MB per image

    // TTL (Time To Live) in hours
    public static final long CACHE_TTL_HOURS = 30L * 24;  // 30 days

    // Download settings
    public static final int DOWNLOAD_TIMEOUT_MS = 10000;  // 10 seconds
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_DELAY_MS = 1000;

    // Performance settings
    public static final int THREAD_POOL_SIZE = 3;  // Background cache downloads

    // Fallback placeholder image
    public static final String PLACEHOLDER_IMAGE_PATH = "/images/placeholder.png";

    // Metadata file extensions
    public static final String METADATA_FILE_EXTENSION = ".meta.json";
}
