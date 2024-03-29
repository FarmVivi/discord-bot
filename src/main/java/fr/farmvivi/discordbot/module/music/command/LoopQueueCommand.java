package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class LoopQueueCommand extends MusicCommand {
    public LoopQueueCommand(MusicModule musicModule) {
        super(musicModule, "loopqueue", CommandCategory.MUSIC, "Répète en boucle la file d'attente");
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

        if (musicModule.getPlayer(guild).isLoopQueueMode()) {
            musicModule.getPlayer(guild).setLoopQueueMode(false);
            reply.success("**Loop queue** désactivé.");
        } else {
            musicModule.getPlayer(guild).setLoopQueueMode(true);
            reply.success("**Loop queue** activé.");
        }

        reply.setEphemeral(true);

        return true;
    }
}
