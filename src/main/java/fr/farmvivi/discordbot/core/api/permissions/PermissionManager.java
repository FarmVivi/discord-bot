package fr.farmvivi.discordbot.core.api.permissions;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

import java.util.Map;
import java.util.Set;

/**
 * Interface for managing permissions.
 * This provides methods to register, check, and modify permissions.
 */
public interface PermissionManager {
    /**
     * Registers a permission with the manager.
     * Permissions must be registered before they can be used.
     *
     * @param permission the permission to register
     * @param owner      the plugin that owns this permission
     */
    void registerPermission(Permission permission, Plugin owner);

    /**
     * Checks if a user has a permission globally.
     *
     * @param userId     the user ID
     * @param permission the permission name
     * @return true if the user has the permission, false otherwise
     */
    boolean hasPermission(String userId, String permission);

    /**
     * Checks if a user has a permission in a specific guild.
     *
     * @param userId     the user ID
     * @param guildId    the guild ID
     * @param permission the permission name
     * @return true if the user has the permission, false otherwise
     */
    boolean hasPermission(String userId, String guildId, String permission);

    /**
     * Sets a global permission for a user.
     *
     * @param userId     the user ID
     * @param permission the permission name
     * @param value      the permission value
     */
    void setPermission(String userId, String permission, boolean value);

    /**
     * Sets a guild-specific permission for a user.
     *
     * @param userId     the user ID
     * @param guildId    the guild ID
     * @param permission the permission name
     * @param value      the permission value
     */
    void setPermission(String userId, String guildId, String permission, boolean value);

    /**
     * Clears all permissions for a user.
     *
     * @param userId the user ID
     */
    void clearPermissions(String userId);

    /**
     * Clears all permissions for a user in a specific guild.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     */
    void clearPermissions(String userId, String guildId);

    /**
     * Gets all registered permissions.
     *
     * @return a set of all registered permissions
     */
    Set<Permission> getRegisteredPermissions();

    /**
     * Gets all permissions for a user.
     *
     * @param userId the user ID
     * @return a map of permission names to values
     */
    Map<String, Boolean> getUserPermissions(String userId);

    /**
     * Gets all permissions for a user in a specific guild.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return a map of permission names to values
     */
    Map<String, Boolean> getUserGuildPermissions(String userId, String guildId);

    /**
     * Gets a registered permission by name.
     *
     * @param name the permission name
     * @return the permission, or null if not found
     */
    Permission getPermission(String name);

    /**
     * Gets all permissions registered by a specific plugin.
     *
     * @param plugin the plugin
     * @return a set of permissions registered by the plugin
     */
    Set<Permission> getPermissions(Plugin plugin);

    /**
     * Unregisters all permissions owned by a plugin.
     * This is typically called when a plugin is disabled.
     *
     * @param plugin the plugin
     * @return the number of permissions unregistered
     */
    int unregisterPermissions(Plugin plugin);
}