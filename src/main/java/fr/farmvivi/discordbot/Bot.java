package fr.farmvivi.discordbot;

import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.ModulesManager;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    public static final String version = "1.4.4";
    public static final String name = "DiscordBot";
    public static final Logger logger = LoggerFactory.getLogger(name);
    private static Bot instance;
    private final Configuration configuration;
    private final ModulesManager modulesManager;

    public Bot() {
        logger.info("Démarrage de " + name + " v" + version + " en cours...");

        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");

        instance = this;

        configuration = new Configuration();
        modulesManager = new ModulesManager(this);

        modulesManager.loadModules();

        setDefaultActivity();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown asked!");

            modulesManager.unloadModules();

            JDAManager.getShardManager().shutdown();

            logger.info("Bye!");
        }));
    }

    public static Bot getInstance() {
        return instance;
    }

    public static void setDefaultActivity() {
        JDAManager.getShardManager()
                .setActivity(Activity
                        .playing("v" + version + " | Prefix: " + Bot.getInstance().getConfiguration().cmdPrefix));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ModulesManager getModulesManager() {
        return modulesManager;
    }
}
