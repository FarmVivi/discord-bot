package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ClearQueueCommand extends MusicCommand {
    public ClearQueueCommand(MusicModule musicModule) {
        super(musicModule, "clear-queue", CommandCategory.MUSIC, "Vide la file d'attente");

        this.setAliases(new String[]{"clear", "clearqueue"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.error("Aucune musique en cours de lecture.");
            return false;
        }

        if (musicModule.getPlayer(guild).getQueueSize() == 0) {
            reply.error("Il n'y a pas de musique dans la file d'attente.");
            return false;
        }

        musicModule.getPlayer(guild).clearQueue();
        reply.success("La liste d'attente à été vidé.");
        reply.setEphemeral(true);

        return true;
    }
}
