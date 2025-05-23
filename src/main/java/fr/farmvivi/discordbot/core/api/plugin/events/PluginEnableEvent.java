package fr.farmvivi.discordbot.core.api.plugin.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Event fired before a plugin is enabled.
 * This event is fired before the plugin's onEnable method is called.
 * This event is cancellable. If it is cancelled, the plugin will not be enabled.
 */
public class PluginEnableEvent extends PluginEvent implements Cancellable {
    private boolean cancelled = false;

    /**
     * Creates a new plugin enable event.
     *
     * @param plugin the plugin being enabled
     */
    public PluginEnableEvent(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
