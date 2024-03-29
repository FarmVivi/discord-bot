package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class LoopCommand extends MusicCommand {
    public LoopCommand(MusicModule musicModule) {
        super(musicModule, "loop", CommandCategory.MUSIC, "Répète en boucle la musique en cours de lecture");
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

        if (musicModule.getPlayer(guild).isLoopMode()) {
            musicModule.getPlayer(guild).setLoopMode(false);
            reply.success("**Loop** désactivé.");
        } else {
            musicModule.getPlayer(guild).setLoopMode(true);
            reply.success("**Loop** activé.");
        }

        reply.setEphemeral(true);

        return true;
    }
}
