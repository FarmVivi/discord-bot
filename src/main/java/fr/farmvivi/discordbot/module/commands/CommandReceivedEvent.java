package fr.farmvivi.discordbot.module.commands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class CommandReceivedEvent {
    private final MessageChannelUnion channel;
    private final ChannelType channelType;
    private final User author;
    private final String command;
    private final boolean fromGuild;

    public CommandReceivedEvent(MessageChannelUnion channel, ChannelType channelType, User author, String command, boolean fromGuild) {
        this.channel = channel;
        this.channelType = channelType;
        this.author = author;
        this.command = command;
        this.fromGuild = fromGuild;
    }

    public MessageChannelUnion getChannel() {
        return channel;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public User getAuthor() {
        return author;
    }

    public String getCommand() {
        return command;
    }

    public boolean isFromGuild() {
        return fromGuild;
    }
}
