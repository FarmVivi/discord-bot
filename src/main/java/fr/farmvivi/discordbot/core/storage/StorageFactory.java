package fr.farmvivi.discordbot.core.storage;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.storage.db.DatabaseDataStorage;
import fr.farmvivi.discordbot.core.storage.file.FileDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Factory for creating storage systems based on configuration.
 */
public class StorageFactory {
    private static final Logger logger = LoggerFactory.getLogger(StorageFactory.class);
    private static final String DEFAULT_DATA_FOLDER = "data";

    /**
     * Creates a data storage manager based on configuration.
     *
     * @param config       the configuration
     * @param eventManager the event manager
     * @return a configured data storage manager
     */
    public static DataStorageManager createStorageManager(Configuration config, EventManager eventManager) {
        String storageType;
        try {
            storageType = config.getString("data.storage.type", "FILE").toUpperCase();
        } catch (Exception e) {
            logger.warn("Failed to read storage type from config, defaulting to FILE", e);
            storageType = "FILE";
        }

        if ("DB".equals(storageType)) {
            try {
                // Validate required DB settings
                validateDatabaseConfig(config);

                // Create database storage
                DatabaseDataStorage dbStorage = new DatabaseDataStorage(config, eventManager);
                logger.info("Using database storage");
                return new DataStorageManager(dbStorage);
            } catch (Exception e) {
                logger.error("Failed to initialize database storage: {}", e.getMessage());
                logger.info("Falling back to file storage");
                return createFileStorageManager(config, eventManager);
            }
        } else {
            // Default to file storage
            return createFileStorageManager(config, eventManager);
        }
    }

    /**
     * Creates a file-based storage manager.
     *
     * @param config       the configuration
     * @param eventManager the event manager
     * @return a file-based storage manager
     */
    private static DataStorageManager createFileStorageManager(Configuration config, EventManager eventManager) {
        String dataFolderPath;
        try {
            dataFolderPath = config.getString("data.storage.file.folder", DEFAULT_DATA_FOLDER);
        } catch (Exception e) {
            logger.debug("Using default data folder: {}", DEFAULT_DATA_FOLDER);
            dataFolderPath = DEFAULT_DATA_FOLDER;
        }

        File storageFolder = new File(dataFolderPath, "storage");
        if (!storageFolder.exists() && !storageFolder.mkdirs()) {
            logger.warn("Failed to create storage folder: {}", storageFolder.getAbsolutePath());
        }

        // Get debounce time from config or use default (2000ms)
        long debounceMs = 2000;
        try {
            debounceMs = config.getInt("data.storage.file.debounce_ms", 2000);
        } catch (Exception ignored) {
            // Use default
        }

        FileDataStorage fileStorage = new FileDataStorage(storageFolder, eventManager, debounceMs);
        logger.info("Using file storage in {}", storageFolder.getAbsolutePath());
        return new DataStorageManager(fileStorage);
    }

    /**
     * Validates database configuration.
     *
     * @param config the configuration
     * @throws ConfigurationException if configuration is invalid
     */
    private static void validateDatabaseConfig(Configuration config) throws ConfigurationException {
        String url = config.getString("data.storage.db.url");
        String username = config.getString("data.storage.db.username");
        String password = config.getString("data.storage.db.password");

        if (url == null || url.isEmpty()) {
            throw new ConfigurationException("Database URL is required");
        }

        if (!url.startsWith("jdbc:")) {
            throw new ConfigurationException("Invalid database URL: " + url);
        }

        logger.info("Database configuration validated successfully");
    }
}