package fr.farmvivi.discordbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration {
    public String jdaToken;
    public String spotifyId;
    public String spotifySecret;
    public String cmdPrefix;
    public List<Long> cmdAdmins;
    public String radioPath;
    public List<String> features;

    public Configuration() {
        try {
            this.loadConfiguration();
        } catch (Exception e) {
            Bot.logger.error("Error loading configuration", e);
        }
    }

    public void loadConfiguration() throws Exception {
        if (!validateConfiguration()) {
            Bot.logger.error("Configuration isn't valid! Please just modify the default configuration !");
            System.exit(1);
            return;
        }

        this.jdaToken = System.getenv("DISCORD_TOKEN");
        this.spotifyId = System.getenv("SPOTIFY_ID");
        this.spotifySecret = System.getenv("SPOTIFY_TOKEN");
        this.cmdPrefix = System.getenv("CMD_PREFIX");
        this.cmdAdmins = new ArrayList<>();
        for (String admin : Arrays.asList(System.getenv("CMD_ADMINS").split(";")))
            cmdAdmins.add(Long.parseLong(admin));
        this.radioPath = System.getenv("RADIO_PATH");
        this.features = new ArrayList<>();
        for (String feature : Arrays.asList(System.getenv("FEATURES").split(";")))
            features.add(feature);
    }

    public boolean validateConfiguration() {
        if (System.getenv("DISCORD_TOKEN") == null)
            return false;
        if (System.getenv("SPOTIFY_ID") == null)
            return false;
        if (System.getenv("SPOTIFY_TOKEN") == null)
            return false;
        if (System.getenv("CMD_PREFIX") == null)
            return false;
        if (System.getenv("CMD_ADMINS") == null)
            return false;
        if (System.getenv("RADIO_PATH") == null)
            return false;
        if (System.getenv("FEATURES") == null)
            return false;

        return true;
    }
}