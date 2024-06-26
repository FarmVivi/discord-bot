package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ReplayCommand extends MusicCommand {
    public ReplayCommand(MusicModule musicModule) {
        super(musicModule, "replay", CommandCategory.MUSIC, "Ajoute la musique en cours de lecture en haut de la file d'attente");
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();
        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();

        if (currentTrack == null) {
            reply.error("Aucune musique en cours de lecture.");
            return false;
        }

        musicModule.getPlayer(guild).playTrackNow(currentTrack.makeClone());
        //reply.addContent(String.format("La piste [%s](%s) va être rejoué.", currentTrack.getInfo().title, currentTrack.getInfo().uri));

        EmbedBuilder embed = reply.createSuccessEmbed();
        embed.setTitle("Musique rejoué");
        if (currentTrack.getInfo().artworkUrl != null) {
            embed.setThumbnail(currentTrack.getInfo().artworkUrl);
        }
        embed.addField("Titre", String.format("[%s](%s)", currentTrack.getInfo().title, currentTrack.getInfo().uri), false);
        reply.addEmbeds(embed.build());

        reply.setEphemeral(true);

        return true;
    }
}
