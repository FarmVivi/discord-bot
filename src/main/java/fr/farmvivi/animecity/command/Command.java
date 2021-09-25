package fr.farmvivi.animecity.command;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
    protected String name = "null";
    protected String[] aliases = new String[0];
    protected String args = "";
    protected boolean guildOnly = true;
    protected boolean adminOnly = false;

    protected final List<Command> subCommands = new ArrayList<>();

    protected boolean execute(MessageReceivedEvent event, String content) {
        if (guildOnly && !event.isFromGuild()) {
            event.getChannel().sendMessage("Cette commande peut seulement être exécuté sur un serveur discord.")
                    .queue();
            return false;
        } else if (adminOnly && !CommandsManager.ADMINS.contains(event.getAuthor().getIdLong())) {
            event.getChannel().sendMessage("Vous n'avez pas la permission d'exécuter cette commande.").queue();
            return false;
        } else if (subCommands.size() > 0 && content.length() > 0) {
            String cmd = content.split(" ")[0];

            for (final Command command : subCommands) {
                final List<String> cmds = new ArrayList<>();
                cmds.add(command.name);
                if (command.aliases.length > 0)
                    for (final String tempCmd : command.aliases)
                        cmds.add(tempCmd);
                if (cmds.contains(cmd.toLowerCase())) {
                    if (command.args.length() > 0) {
                        final int commandLength = cmd.length() + 1;
                        if (content.length() > commandLength) {
                            command.execute(event, content.substring(commandLength));
                            return false;
                        }
                    }
                    command.execute(event, "");
                    return false;
                }
            }
        }
        return true;
    };
}
