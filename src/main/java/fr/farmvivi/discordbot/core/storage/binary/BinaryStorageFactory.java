package fr.farmvivi.discordbot.core.storage.binary;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageManager;
import fr.farmvivi.discordbot.core.storage.binary.file.FileBinaryStorage;
import fr.farmvivi.discordbot.core.storage.binary.s3.S3BinaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Factory for creating binary storage systems based on configuration.
 */
public class BinaryStorageFactory {
    private static final Logger logger = LoggerFactory.getLogger(BinaryStorageFactory.class);
    private static final String DEFAULT_BINARY_FOLDER = "binary";

    /**
     * Creates a binary storage manager based on configuration.
     *
     * @param config       the configuration
     * @param eventManager the event manager
     * @return a configured binary storage manager
     */
    public static BinaryStorageManager createBinaryStorageManager(Configuration config, EventManager eventManager) {
        String storageType;
        try {
            storageType = config.getString("data.binary.storage.type", "FILE").toUpperCase();
        } catch (Exception e) {
            logger.warn("Failed to read binary storage type from config, defaulting to FILE", e);
            storageType = "FILE";
        }

        if ("S3".equals(storageType)) {
            try {
                // Validate required S3 settings
                validateS3Config(config);

                // Get the S3 configuration
                String bucketName = config.getString("data.binary.storage.s3.bucket");
                String region = config.getString("data.binary.storage.s3.region");
                String accessKey = config.getString("data.binary.storage.s3.access_key");
                String secretKey = config.getString("data.binary.storage.s3.secret_key");
                String endpoint = config.getString("data.binary.storage.s3.endpoint", "");
                String prefix = config.getString("data.binary.storage.s3.prefix", "");

                // Create S3 storage
                S3BinaryStorage s3Storage = new S3BinaryStorage("s3", bucketName, prefix, endpoint, region, accessKey, secretKey, eventManager);
                logger.info("Using S3 binary storage with bucket {}", bucketName);
                return new BinaryStorageManager(s3Storage);
            } catch (Exception e) {
                logger.error("Failed to initialize S3 binary storage: {}", e.getMessage());
                logger.info("Falling back to file binary storage");
                return createFileBinaryStorageManager(config, eventManager);
            }
        } else {
            // Default to file storage
            return createFileBinaryStorageManager(config, eventManager);
        }
    }

    /**
     * Creates a file-based binary storage manager.
     *
     * @param config       the configuration
     * @param eventManager the event manager
     * @return a file-based binary storage manager
     */
    private static BinaryStorageManager createFileBinaryStorageManager(Configuration config, EventManager eventManager) {
        String binaryFolderPath;
        try {
            binaryFolderPath = config.getString("data.binary.storage.file.folder", DEFAULT_BINARY_FOLDER);
        } catch (Exception e) {
            logger.debug("Using default binary folder: {}", DEFAULT_BINARY_FOLDER);
            binaryFolderPath = DEFAULT_BINARY_FOLDER;
        }

        File binaryFolder = new File(binaryFolderPath);
        if (!binaryFolder.exists() && !binaryFolder.mkdirs()) {
            logger.warn("Failed to create binary folder: {}", binaryFolder.getAbsolutePath());
        }

        FileBinaryStorage fileBinaryStorage = new FileBinaryStorage("file", binaryFolder, eventManager);
        logger.info("Using file binary storage in {}", binaryFolder.getAbsolutePath());
        return new BinaryStorageManager(fileBinaryStorage);
    }

    /**
     * Validates S3 configuration.
     *
     * @param config the configuration
     * @throws ConfigurationException if configuration is invalid
     */
    private static void validateS3Config(Configuration config) throws ConfigurationException {
        String bucketName = config.getString("data.binary.storage.s3.bucket");
        String region = config.getString("data.binary.storage.s3.region");
        String accessKey = config.getString("data.binary.storage.s3.access_key");
        String secretKey = config.getString("data.binary.storage.s3.secret_key");

        if (bucketName == null || bucketName.isEmpty()) {
            throw new ConfigurationException("S3 bucket name is required");
        }

        if (region == null || region.isEmpty()) {
            throw new ConfigurationException("S3 region is required");
        }

        if (accessKey == null || accessKey.isEmpty()) {
            throw new ConfigurationException("S3 access key is required");
        }

        if (secretKey == null || secretKey.isEmpty()) {
            throw new ConfigurationException("S3 secret key is required");
        }

        logger.info("S3 configuration validated successfully");
    }
}