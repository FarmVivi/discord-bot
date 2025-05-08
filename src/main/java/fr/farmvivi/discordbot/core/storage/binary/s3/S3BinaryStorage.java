package fr.farmvivi.discordbot.core.storage.binary.s3;

import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageKey;
import fr.farmvivi.discordbot.core.storage.binary.AbstractBinaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BinaryStorage using Amazon S3 or compatible services.
 */
public class S3BinaryStorage extends AbstractBinaryStorage {
    private static final Logger logger = LoggerFactory.getLogger(S3BinaryStorage.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String rootPrefix;

    /**
     * Creates a new S3 binary storage.
     *
     * @param storageName  the name of the storage
     * @param bucketName   the S3 bucket name
     * @param rootPrefix   the root prefix for all objects (can be empty)
     * @param endpoint     the S3 endpoint URL (null for AWS)
     * @param region       the AWS region
     * @param accessKey    the AWS access key
     * @param secretKey    the AWS secret key
     * @param eventManager the event manager
     */
    public S3BinaryStorage(String storageName, String bucketName, String rootPrefix,
                           String endpoint, String region, String accessKey, String secretKey,
                           EventManager eventManager) {
        super(storageName, eventManager);
        this.bucketName = bucketName;
        this.rootPrefix = normalizePrefix(rootPrefix);

        // Build S3 client
        S3ClientBuilder clientBuilder = S3Client.builder();
        S3Presigner.Builder presignerBuilder = S3Presigner.builder();

        // Set region
        Region awsRegion = Region.of(region);
        clientBuilder.region(awsRegion);
        presignerBuilder.region(awsRegion);

        // Set credentials
        if (accessKey != null && secretKey != null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            clientBuilder.credentialsProvider(credentialsProvider);
            presignerBuilder.credentialsProvider(credentialsProvider);
        }

        // Set custom endpoint for S3-compatible services
        if (endpoint != null && !endpoint.isEmpty()) {
            clientBuilder.endpointOverride(URI.create(endpoint));
            presignerBuilder.endpointOverride(URI.create(endpoint));
        }

        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();

        // Ensure bucket exists
        ensureBucketExists();
    }

    /**
     * Normalizes a prefix to ensure it ends with a slash if not empty.
     *
     * @param prefix the prefix to normalize
     * @return the normalized prefix
     */
    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }

        // Replace backslashes with forward slashes
        String normalized = prefix.replace('\\', '/');

        // Remove leading slash if present
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // Add trailing slash if not present
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized;
    }

    /**
     * Ensures the S3 bucket exists, creating it if necessary.
     */
    private void ensureBucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
        } catch (NoSuchBucketException e) {
            logger.info("[{}] Bucket {} does not exist, creating it", storageName, bucketName);
            try {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);
            } catch (Exception ex) {
                logger.error("[{}] Failed to create bucket {}: {}",
                        storageName, bucketName, ex.getMessage(), ex);
            }
        } catch (Exception e) {
            logger.error("[{}] Error checking if bucket {} exists: {}",
                    storageName, bucketName, e.getMessage(), e);
        }
    }

    @Override
    public OutputStream getOutputStream(BinaryStorageKey key, boolean overwrite) {
        String objectKey = getObjectKey(key);

        if (!overwrite && fileExists(key)) {
            logger.warn("[{}] File already exists at path {} and overwrite is false",
                    storageName, key.getFullPath());
            return null;
        }

        // Since S3 doesn't provide a direct output stream, use a pipe stream
        PipedOutputStream outputStream = new PipedOutputStream();
        try {
            final PipedInputStream inputStream = new PipedInputStream(outputStream);
            final String finalObjectKey = objectKey;

            // Start a separate thread to handle the upload to S3
            new Thread(() -> {
                try {
                    // Upload the content to S3
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(finalObjectKey)
                            .contentType(getContentType(key))
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, -1));
                } catch (Exception e) {
                    logger.error("[{}] Error uploading to S3 for path {}: {}",
                            storageName, key.getFullPath(), e.getMessage(), e);
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("[{}] Error closing input stream for path {}: {}",
                                storageName, key.getFullPath(), e.getMessage());
                    }
                }
            }).start();

            return outputStream;
        } catch (IOException e) {
            logger.error("[{}] Error creating output stream for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<InputStream> getInputStream(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        try {
            // Check if the object exists first
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                logger.debug("[{}] File does not exist at path: {}", storageName, key.getFullPath());
                return Optional.empty();
            }

            // Get the object
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            return Optional.of(s3Client.getObject(getObjectRequest));
        } catch (Exception e) {
            logger.error("[{}] Error getting input stream for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean fileExists(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("[{}] Error checking if file exists at path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected boolean doDeleteFile(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (Exception e) {
            logger.error("[{}] Error deleting file at path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<String> listFiles(BinaryStorageKey key) {
        String prefix = getObjectKey(key);

        // Ensure directory path ends with a slash
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        List<String> files = new ArrayList<>();

        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .delimiter("/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);

            // Extract file names, removing the directory prefix
            for (S3Object s3Object : response.contents()) {
                String objectKey = s3Object.key();

                // Skip the directory itself
                if (!objectKey.equals(prefix)) {
                    // Remove the prefix part to get the relative path
                    String relativePath = removeFullPrefix(objectKey, key.scope() + "/");
                    files.add(relativePath);
                }
            }
        } catch (Exception e) {
            logger.error("[{}] Error listing files in directory {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
        }

        return files;
    }

    @Override
    public long getFileSize(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
        } catch (NoSuchKeyException e) {
            return -1;
        } catch (Exception e) {
            logger.error("[{}] Error getting file size for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return -1;
        }
    }

    @Override
    public long getLastModifiedTime(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.lastModified().toEpochMilli();
        } catch (NoSuchKeyException e) {
            return -1;
        } catch (Exception e) {
            logger.error("[{}] Error getting last modified time for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return -1;
        }
    }

    @Override
    public boolean createDirectory(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        // S3 doesn't have real directories, but we can create an empty object with trailing slash
        if (!objectKey.endsWith("/")) {
            objectKey += "/";
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.empty());
            return true;
        } catch (Exception e) {
            logger.error("[{}] Error creating directory at path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isDirectory(BinaryStorageKey key) {
        String objectKey = getObjectKey(key);

        if (!objectKey.endsWith("/")) {
            objectKey += "/";
        }

        try {
            // Check if there's an empty object with trailing slash
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
                return true;
            } catch (NoSuchKeyException e) {
                // If the object doesn't exist, check if there are objects with this prefix
                ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(objectKey)
                        .maxKeys(1)
                        .build();

                ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
                return response.hasContents();
            }
        } catch (Exception e) {
            logger.error("[{}] Error checking if path {} is a directory: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Optional<String> getPublicUrl(BinaryStorageKey key, int expireIn) {
        String objectKey = getObjectKey(key);

        try {
            // Check if the object exists first
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                logger.debug("[{}] File does not exist at path: {}", storageName, key.getFullPath());
                return Optional.empty();
            }

            // Create a presigned request for the object
            int expirationSeconds = (expireIn > 0) ? expireIn : 3600; // Default to 1 hour

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationSeconds))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build())
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            return Optional.of(presignedRequest.url().toString());
        } catch (Exception e) {
            logger.error("[{}] Error generating public URL for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean close() {
        if (s3Client != null) {
            s3Client.close();
        }

        if (s3Presigner != null) {
            s3Presigner.close();
        }

        return true;
    }

    /**
     * Gets the full S3 object key for a storage key.
     *
     * @param key the storage key
     * @return the S3 object key
     */
    private String getObjectKey(BinaryStorageKey key) {
        String fullPath = key.getFullPath();

        if (rootPrefix.isEmpty()) {
            return fullPath;
        }

        return rootPrefix + fullPath;
    }

    /**
     * Removes a prefix from an S3 object key.
     *
     * @param objectKey the S3 object key
     * @param prefix    the prefix to remove
     * @return the path without the prefix
     */
    private String removeFullPrefix(String objectKey, String prefix) {
        String fullPrefix = rootPrefix + prefix;

        if (objectKey.startsWith(fullPrefix)) {
            return objectKey.substring(fullPrefix.length());
        }

        return objectKey;
    }
}