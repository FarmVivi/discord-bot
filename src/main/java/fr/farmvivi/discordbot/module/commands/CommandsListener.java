package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Configuration;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandsListener extends ListenerAdapter {
    private final CommandsModule commandsModule;
    private final Configuration botConfig;

    public CommandsListener(CommandsModule commandsModule, Configuration botConfig) {
        this.commandsModule = commandsModule;
        this.botConfig = botConfig;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            String message = event.getMessage().getContentDisplay();
            String CMD_PREFIX = botConfig.cmdPrefix;

            if (!message.startsWith(CMD_PREFIX))
                return;

            String cmd = message.substring(CMD_PREFIX.length()).split(" ")[0];

            for (Command command : commandsModule.getCommands()) {
                List<String> commands = new ArrayList<>();
                commands.add(command.name);
                if (command.aliases.length != 0)
                    Collections.addAll(commands, command.aliases);
                if (commands.contains(cmd.toLowerCase())) {
                    if (command.args.length() != 0) {
                        int commandLength = CMD_PREFIX.length() + cmd.length() + 1;
                        if (message.length() > commandLength) {
                            command.execute(event, message.substring(commandLength));
                            return;
                        }
                    }
                    command.execute(event, "");
                    return;
                }
            }
        }
    }
}
