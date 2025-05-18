package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandBuilder;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import fr.farmvivi.discordbot.core.command.option.SimpleCommandOption;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandBuilder for creating commands with a fluent API.
 */
public class SimpleCommandBuilder implements CommandBuilder {

    private final SimpleCommand.Builder builder = new SimpleCommand.Builder();
    private final List<SimpleCommandBuilder> subcommandBuilders = new ArrayList<>();

    @Override
    public CommandBuilder name(String name) {
        builder.name(name);
        return this;
    }

    @Override
    public CommandBuilder description(String description) {
        builder.description(description);
        return this;
    }

    @Override
    public CommandBuilder category(String category) {
        builder.category(category);
        return this;
    }

    @Override
    public CommandBuilder group(String group) {
        builder.group(group);
        return this;
    }

    @Override
    public CommandBuilder permission(String permission) {
        builder.permission(permission);
        return this;
    }

    @Override
    public CommandBuilder translationKey(String translationKey) {
        builder.translationKey(translationKey);
        return this;
    }

    @Override
    public CommandBuilder alias(String alias) {
        builder.alias(alias);
        return this;
    }

    @Override
    public CommandBuilder aliases(String... aliases) {
        builder.aliases(aliases);
        return this;
    }

    @Override
    public CommandBuilder guildOnly(boolean guildOnly) {
        builder.guildOnly(guildOnly);
        return this;
    }

    @Override
    public CommandBuilder guilds(String... guildIds) {
        builder.guildIds(guildIds);
        return this;
    }

    @Override
    public CommandBuilder enabled(boolean enabled) {
        builder.enabled(enabled);
        return this;
    }

    @Override
    public CommandBuilder cooldown(int cooldown) {
        builder.cooldown(cooldown);
        return this;
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<String>()
                .name(name)
                .description(description)
                .type(OptionType2.STRING)
                .required(required)
                .build());
    }

    @Override
    @SafeVarargs
    public final CommandBuilder stringOption(String name, String description, boolean required, OptionChoice<String>... choices) {
        return option(new SimpleCommandOption.Builder<String>()
                .name(name)
                .description(description)
                .type(OptionType2.STRING)
                .required(required)
                .choices(List.of(choices))
                .build());
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required, Predicate<String> validator) {
        return option(new SimpleCommandOption.Builder<String>()
                .name(name)
                .description(description)
                .type(OptionType2.STRING)
                .required(required)
                .validator(validator)
                .build());
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required, Function<String, List<OptionChoice<String>>> autocompleteProvider) {
        return option(new SimpleCommandOption.Builder<String>()
                .name(name)
                .description(description)
                .type(OptionType2.STRING)
                .required(required)
                .autocompleteProvider(autocompleteProvider)
                .build());
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Integer>()
                .name(name)
                .description(description)
                .type(OptionType2.INTEGER)
                .required(required)
                .build());
    }

    @Override
    @SafeVarargs
    public final CommandBuilder integerOption(String name, String description, boolean required, OptionChoice<Integer>... choices) {
        return option(new SimpleCommandOption.Builder<Integer>()
                .name(name)
                .description(description)
                .type(OptionType2.INTEGER)
                .required(required)
                .choices(List.of(choices))
                .build());
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required, Predicate<Integer> validator) {
        return option(new SimpleCommandOption.Builder<Integer>()
                .name(name)
                .description(description)
                .type(OptionType2.INTEGER)
                .required(required)
                .validator(validator)
                .build());
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required, Integer min, Integer max) {
        return option(new SimpleCommandOption.Builder<Integer>()
                .name(name)
                .description(description)
                .type(OptionType2.INTEGER)
                .required(required)
                .minValue(min)
                .maxValue(max)
                .build());
    }

    @Override
    public CommandBuilder booleanOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Boolean>()
                .name(name)
                .description(description)
                .type(OptionType2.BOOLEAN)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder userOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Object>()
                .name(name)
                .description(description)
                .type(OptionType2.USER)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder channelOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Object>()
                .name(name)
                .description(description)
                .type(OptionType2.CHANNEL)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder roleOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Object>()
                .name(name)
                .description(description)
                .type(OptionType2.ROLE)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder mentionableOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Object>()
                .name(name)
                .description(description)
                .type(OptionType2.MENTIONABLE)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder numberOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Double>()
                .name(name)
                .description(description)
                .type(OptionType2.NUMBER)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder numberOption(String name, String description, boolean required, Double min, Double max) {
        return option(new SimpleCommandOption.Builder<Double>()
                .name(name)
                .description(description)
                .type(OptionType2.NUMBER)
                .required(required)
                .minValue(min)
                .maxValue(max)
                .build());
    }

    @Override
    public CommandBuilder attachmentOption(String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Object>()
                .name(name)
                .description(description)
                .type(OptionType2.ATTACHMENT)
                .required(required)
                .build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandBuilder option(OptionType2 type, String name, String description, boolean required) {
        return option(new SimpleCommandOption.Builder<Object>()
                .name(name)
                .description(description)
                .type(type)
                .required(required)
                .build());
    }

    @Override
    public CommandBuilder option(CommandOption<?> option) {
        builder.option(option);
        return this;
    }

    @Override
    public CommandBuilder subcommand(Consumer<CommandBuilder> subcommandConsumer) {
        SimpleCommandBuilder subcommandBuilder = new SimpleCommandBuilder();
        subcommandBuilder.builder.subcommand(true);
        subcommandConsumer.accept(subcommandBuilder);
        subcommandBuilders.add(subcommandBuilder);
        return this;
    }

    @Override
    public CommandBuilder executor(BiFunction<CommandContext, Command, CommandResult> executor) {
        builder.executor(executor);
        return this;
    }

    @Override
    public CommandBuilder execute(BiFunction<CommandContext, Command, CommandResult> executor) {
        return executor(executor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Command build() {
        // Process subcommands first
        for (SimpleCommandBuilder subcommandBuilder : subcommandBuilders) {
            Command subcommand = subcommandBuilder.build();
            builder.subcommand(subcommand);
        }

        SimpleCommand command = builder.build();
        
        // Set parent for all subcommands
        for (Command subcommand : command.subcommands()) {
            if (subcommand instanceof SimpleCommand) {
                SimpleCommand simpleSubcommand = (SimpleCommand) subcommand;
                SimpleCommand.Builder subcommandBuilder = new SimpleCommand.Builder()
                        .name(simpleSubcommand.name())
                        .description(simpleSubcommand.description())
                        .category(simpleSubcommand.category());
                
                // Copy all fields
                for (CommandOption<?> option : simpleSubcommand.options()) {
                    subcommandBuilder.option(option);
                }
                for (Command nestedSubcommand : simpleSubcommand.subcommands()) {
                    subcommandBuilder.subcommand(nestedSubcommand);
                }
                
                subcommandBuilder
                        .group(simpleSubcommand.group())
                        .permission(simpleSubcommand.permission())
                        .translationKey(simpleSubcommand.translationKey());
                
                for (String alias : simpleSubcommand.aliases()) {
                    subcommandBuilder.alias(alias);
                }
                
                subcommandBuilder
                        .guildOnly(simpleSubcommand.isGuildOnly());
                
                for (String guildId : simpleSubcommand.guildIds()) {
                    subcommandBuilder.guildId(guildId);
                }
                
                subcommandBuilder
                        .subcommand(true)
                        .parent(command)
                        .enabled(simpleSubcommand.isEnabled())
                        .cooldown(simpleSubcommand.getCooldown())
                        .executor(simpleSubcommand.executor());
            }
        }
        
        return command;
    }
}
