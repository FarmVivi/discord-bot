package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Configuration;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        StringBuilder options = new StringBuilder();
        for (OptionMapping option : event.getOptions()) {
            options.append(" ").append(option.getAsString());
        }
        String content = options.toString().replaceFirst(" ", "");

        String cmd = event.getName();

        CommandReceivedEvent commandReceivedEvent = new CommandReceivedEvent(
                event.getGuild(),
                event.getChannel(),
                event.getChannelType(),
                event.getUser(),
                cmd,
                event.isFromGuild());

        CommandMessageBuilder reply = new CommandMessageBuilder(event);

        for (Command command : commandsModule.getCommands()) {
            if (command.getName().equalsIgnoreCase(cmd)) {
                if (command.getArgs().length != 0 && !content.isBlank()) {
                    command.execute(commandReceivedEvent, content, reply);
                } else {
                    command.execute(commandReceivedEvent, "", reply);
                }

                if (reply.isDiffer()) {
                    event.deferReply().queue();
                } else {
                    event.reply(reply.build()).queue();
                }
                return;
            }
        }

        event.reply("> Une erreur est survenue, la commande est inconnue :confused:").setEphemeral(true).queue();
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

            CommandReceivedEvent commandReceivedEvent = new CommandReceivedEvent(
                    event.getGuild(),
                    event.getChannel(),
                    event.getChannelType(),
                    event.getAuthor(),
                    cmd,
                    event.isFromGuild());

            CommandMessageBuilder reply = new CommandMessageBuilder(event);
            reply.append("> **Cette commande est obsolète !**\n")
                    .append("> Veuillez utiliser les commandes en commençant par un **/** au lieu de **").append(botConfig.cmdPrefix).append("** !")
                    .append("\n")
                    .append("\n");

            for (Command command : commandsModule.getCommands()) {
                List<String> commands = new ArrayList<>();
                commands.add(command.getName());
                if (command.getAliases().length != 0)
                    Collections.addAll(commands, command.getAliases());
                if (commands.contains(cmd.toLowerCase())) {
                    if (command.getArgs().length != 0) {
                        int commandLength = CMD_PREFIX.length() + cmd.length() + 1;
                        if (message.length() > commandLength) {
                            command.execute(commandReceivedEvent, message.substring(commandLength), reply);
                            if (!reply.isDiffer()) {
                                event.getMessage().reply(reply.build()).queue();
                            }
                            return;
                        }
                    }
                    command.execute(commandReceivedEvent, "", reply);
                    if (!reply.isDiffer()) {
                        event.getMessage().reply(reply.build()).queue();
                    }
                    return;
                }
            }
        }
    }
}
