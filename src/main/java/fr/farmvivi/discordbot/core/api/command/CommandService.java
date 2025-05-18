package fr.farmvivi.discordbot.core.api.command;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for managing commands.
 * Provides methods for registering, executing, and synchronizing commands.
 */
public interface CommandService {

    /**
     * Gets the command registry.
     *
     * @return the command registry
     */
    CommandRegistry getRegistry();

    /**
     * Gets the command builder for creating new commands.
     *
     * @return a new command builder
     */
    CommandBuilder newCommand();

    /**
     * Gets the prefix for text commands.
     *
     * @return the command prefix
     */
    String getPrefix();

    /**
     * Gets the prefix for text commands in a specific guild.
     *
     * @param guildId the guild ID
     * @return the command prefix for the guild
     */
    String getPrefix(String guildId);

    /**
     * Sets the prefix for text commands.
     *
     * @param prefix the new command prefix
     */
    void setPrefix(String prefix);

    /**
     * Sets the prefix for text commands in a specific guild.
     *
     * @param guildId the guild ID
     * @param prefix  the new command prefix for the guild
     */
    void setPrefix(String guildId, String prefix);

    /**
     * Registers a command.
     *
     * @param command the command to register
     * @param plugin  the plugin registering the command
     * @return true if the command was registered
     */
    boolean registerCommand(Command command, Plugin plugin);

    /**
     * Creates and registers a command using a builder.
     *
     * @param plugin         the plugin registering the command
     * @param builderConsumer consumer to configure the command builder
     * @return true if the command was registered
     */
    boolean registerCommand(Plugin plugin, Consumer<CommandBuilder> builderConsumer);

    /**
     * Synchronizes all commands with Discord.
     * This updates the slash commands available to users.
     *
     * @return a future that completes when the synchronization is done
     */
    CompletableFuture<Void> synchronizeCommands();

    /**
     * Synchronizes guild commands with Discord.
     * This updates the slash commands available to users in a specific guild.
     *
     * @param guild the guild
     * @return a future that completes when the synchronization is done
     */
    CompletableFuture<Void> synchronizeGuildCommands(Guild guild);

    /**
     * Synchronizes global commands with Discord.
     * This updates the slash commands available to users in all guilds.
     *
     * @return a future that completes when the synchronization is done
     */
    CompletableFuture<Void> synchronizeGlobalCommands();

    /**
     * Enables the command service.
     * This registers the event listeners and initializes the service.
     */
    void enable();

    /**
     * Disables the command service.
     * This unregisters the event listeners and cleans up resources.
     */
    void disable();

    /**
     * Sets the JDA instance to use for command handling.
     *
     * @param jda the JDA instance
     */
    void setJDA(JDA jda);

    /**
     * Gets the JDA instance.
     *
     * @return the JDA instance
     */
    JDA getJDA();

    /**
     * Checks if the service is enabled.
     *
     * @return true if the service is enabled
     */
    boolean isEnabled();

    /**
     * Gets the number of commands executed since the service was enabled.
     *
     * @return the number of executed commands
     */
    long getCommandExecutionCount();

    /**
     * Gets the number of commands successfully executed since the service was enabled.
     *
     * @return the number of successfully executed commands
     */
    long getSuccessfulCommandExecutionCount();

    /**
     * Gets the number of commands that failed execution since the service was enabled.
     *
     * @return the number of failed command executions
     */
    long getFailedCommandExecutionCount();

    /**
     * Gets the average command execution time in milliseconds.
     *
     * @return the average execution time
     */
    double getAverageExecutionTimeMs();

    /**
     * Checks if a user is on cooldown for a specific command.
     *
     * @param userId    the user ID
     * @param commandName the command name
     * @return true if the user is on cooldown
     */
    boolean isOnCooldown(String userId, String commandName);

    /**
     * Gets the remaining cooldown time in seconds for a user and command.
     *
     * @param userId    the user ID
     * @param commandName the command name
     * @return the remaining cooldown time in seconds, or 0 if not on cooldown
     */
    int getRemainingCooldown(String userId, String commandName);
}
