package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class NextCommand extends Command {
    private final MusicModule musicModule;

    public NextCommand(MusicModule musicModule) {
        super("next", CommandCategory.MUSIC, "Passe à la musique suivante (reste dans la file d'attente si loopqueue est activé)");

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.append("Aucune musique en cours de lecture.");
            return false;
        }

        AudioTrack track = musicModule.getPlayer(guild).nextTrack();
        if (track == null) {
            reply.append("Plus aucune musique à jouer.");
        } else {
            reply.append("Musique suivante: **").append(track.getInfo().title).append("**");
        }

        return true;
    }
}
