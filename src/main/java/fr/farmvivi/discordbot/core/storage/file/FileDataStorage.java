package fr.farmvivi.discordbot.core.storage.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.StorageKey;
import fr.farmvivi.discordbot.core.storage.AbstractDataStorage;
import fr.farmvivi.discordbot.core.util.Debouncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based implementation of DataStorage.
 * Stores data in JSON files organized by scope.
 */
public class FileDataStorage extends AbstractDataStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileDataStorage.class);
    private static final String DATA_FILENAME = "data.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final File baseDirectory;
    private final Map<String, Debouncer> saveThrottlers = new ConcurrentHashMap<>();
    private final long saveDebounceMs;

    /**
     * Creates a new file data storage.
     *
     * @param baseDirectory  the base directory for storing data files
     * @param eventManager   the event manager
     * @param saveDebounceMs debounce time for saving in milliseconds (to avoid too frequent writes)
     */
    public FileDataStorage(File baseDirectory, EventManager eventManager, long saveDebounceMs) {
        super("file", eventManager);
        this.baseDirectory = baseDirectory;
        this.saveDebounceMs = saveDebounceMs;

        if (!baseDirectory.exists()) {
            if (!baseDirectory.mkdirs()) {
                logger.error("Failed to create base directory: {}", baseDirectory.getAbsolutePath());
            }
        }
    }

    @Override
    protected <T> Optional<T> doGet(StorageKey key, Class<T> type) {
        String scope = key.getScope();
        String keyName = key.getKey();

        // Get the scope data
        Map<String, Object> scopeData = loadScopeData(scope);
        if (scopeData.containsKey(keyName)) {
            try {
                // Convert object to the requested type using Gson
                String json = gson.toJson(scopeData.get(keyName));
                T value = gson.fromJson(json, type);
                return Optional.ofNullable(value);
            } catch (Exception e) {
                logger.error("[{}] Error converting data for key {} in scope {}: {}",
                        storageType, keyName, scope, e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    protected <T> boolean doSet(StorageKey key, T value) {
        String scope = key.getScope();
        String keyName = key.getKey();

        // Get or load the scope data
        Map<String, Object> scopeData = loadScopeData(scope);

        // Set the value
        scopeData.put(keyName, value);

        // Schedule a save
        scheduleSave(scope);

        return true;
    }

    @Override
    protected boolean doExists(StorageKey key) {
        String scope = key.getScope();
        String keyName = key.getKey();

        // Check if the key exists in the scope data
        Map<String, Object> scopeData = loadScopeData(scope);
        return scopeData.containsKey(keyName);
    }

    @Override
    protected boolean doRemove(StorageKey key) {
        String scope = key.getScope();
        String keyName = key.getKey();

        // Get the scope data
        Map<String, Object> scopeData = loadScopeData(scope);
        if (scopeData.containsKey(keyName)) {
            // Remove the key
            scopeData.remove(keyName);

            // Schedule a save
            scheduleSave(scope);

            return true;
        }

        return false;
    }

    @Override
    protected Set<String> doGetKeys(String scope) {
        // Get the scope data
        Map<String, Object> scopeData = loadScopeData(scope);
        return new HashSet<>(scopeData.keySet());
    }

    @Override
    protected Map<String, Object> doGetAll(String scope) {
        // Get the scope data
        return new HashMap<>(loadScopeData(scope));
    }

    @Override
    protected boolean doClear(String scope) {
        // Clear the scope data
        File scopeDir = getScopeDirectory(scope);
        File dataFile = new File(scopeDir, DATA_FILENAME);

        if (dataFile.exists()) {
            if (!dataFile.delete()) {
                logger.error("[{}] Failed to delete data file for scope: {}", storageType, scope);
                return false;
            }
        }

        // If scope directory is empty, delete it too
        if (scopeDir.exists() && scopeDir.list() != null && scopeDir.list().length == 0) {
            if (!scopeDir.delete()) {
                logger.warn("[{}] Failed to delete empty scope directory: {}", storageType, scope);
            }
        }

        return true;
    }

    @Override
    public boolean save() {
        boolean success = true;

        // Save all scopes
        for (String scope : new HashSet<>(cache.keySet())) {
            if (!saveScopeData(scope)) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean close() {
        // Save all pending data
        boolean result = save();

        // Shutdown all debouncers
        for (Debouncer debouncer : saveThrottlers.values()) {
            debouncer.shutdown();
        }
        saveThrottlers.clear();

        return result;
    }

    /**
     * Gets the directory for a specific scope.
     *
     * @param scope the scope
     * @return the scope directory
     */
    private File getScopeDirectory(String scope) {
        // Convert scope to directory path
        String dirPath = scope.replace(':', '/');
        File scopeDir = new File(baseDirectory, dirPath);

        // Create directory if it doesn't exist
        if (!scopeDir.exists()) {
            if (!scopeDir.mkdirs()) {
                logger.error("[{}] Failed to create scope directory: {}", storageType, scope);
            }
        }

        return scopeDir;
    }

    /**
     * Loads data for a specific scope.
     *
     * @param scope the scope
     * @return the scope data
     */
    private Map<String, Object> loadScopeData(String scope) {
        // Check cache first
        Map<String, Object> cachedData = cache.get(scope);
        if (cachedData != null) {
            return cachedData;
        }

        // Load from file
        File scopeDir = getScopeDirectory(scope);
        File dataFile = new File(scopeDir, DATA_FILENAME);

        Map<String, Object> data = new HashMap<>();

        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Map<String, Object> loadedData = gson.fromJson(reader, MAP_TYPE);
                if (loadedData != null) {
                    data.putAll(loadedData);
                }
            } catch (IOException e) {
                logger.error("[{}] Error loading data for scope {}: {}",
                        storageType, scope, e.getMessage());
            }
        }

        // Cache the loaded data
        cache.put(scope, data);

        return data;
    }

    /**
     * Saves data for a specific scope.
     *
     * @param scope the scope
     * @return true if the save was successful
     */
    private boolean saveScopeData(String scope) {
        Map<String, Object> scopeData = cache.get(scope);
        if (scopeData == null || scopeData.isEmpty()) {
            return true; // Nothing to save
        }

        File scopeDir = getScopeDirectory(scope);
        File dataFile = new File(scopeDir, DATA_FILENAME);

        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(scopeData, writer);
            return true;
        } catch (IOException e) {
            logger.error("[{}] Error saving data for scope {}: {}",
                    storageType, scope, e.getMessage());
            return false;
        }
    }

    /**
     * Schedules a save operation for a specific scope with debouncing.
     *
     * @param scope the scope
     */
    private void scheduleSave(String scope) {
        // Get or create a debouncer for this scope
        Debouncer debouncer = saveThrottlers.computeIfAbsent(scope,
                s -> new Debouncer(saveDebounceMs, () -> saveScopeData(s)));

        // Trigger the debouncer
        debouncer.debounce();
    }
}