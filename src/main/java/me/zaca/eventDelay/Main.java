package me.zaca.eventDelay;

import lombok.Getter;
import lombok.Setter;
import me.zaca.eventDelay.commands.EventCMD;
import me.zaca.eventDelay.configurations.Config;
import me.zaca.eventDelay.configurations.Messages;
import me.zaca.eventDelay.configurations.WebhookConfig;
import me.zaca.eventDelay.manager.Assistants;
import me.zaca.eventDelay.manager.Timer;
import me.zaca.eventDelay.manager.Triggers;
import me.zaca.eventDelay.tools.EventDelayAPI;
import me.zaca.eventDelay.tools.EventDelayExpansion;
import me.zaca.eventDelay.tools.Logger;
import me.zaca.eventDelay.tools.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


@Getter
public final class Main extends JavaPlugin {

    private EventDelayAPI eventDelayAPI;
    private Timer timer;
    private Triggers triggers;
    @Setter
    private Config cfg;
    private Assistants assistants;
    private final WebhookConfig webhookConfig = new WebhookConfig(this);
    private final Messages messages = new Messages(this);

    @Override
    public void onEnable() {

        cfg = new Config(this);
        cfg.load();

        Logger.success("Looking for updates..");
        Version version = new Version(this);
        for (String str : version.getAlert()) {
            Logger.success(str);
        }

        getServer().getPluginManager().registerEvents(version, this);

        final FileConfiguration messagesFile = cfg.getFileConfiguration(getDataFolder().getAbsolutePath(), "messages.yml");
        final FileConfiguration webhookFile = cfg.getFileConfiguration(getDataFolder().getAbsolutePath(), "webhook.yml");


        messages.load(messagesFile);
        webhookConfig.load(webhookFile);


        eventDelayAPI = new EventDelayAPI(cfg.isFreeze(),
                cfg.getTimer(),
                cfg.getMinPlayers(),
                "none",
                "none",
                "none");

        assistants = new Assistants(this, eventDelayAPI);
        triggers = new Triggers(this);
        timer = new Timer(this);
        timer.initialize();
        timer.startTimer();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EventDelayExpansion(this).register();
        }
        getCommand("event").setExecutor(new EventCMD(this));

        Bukkit.getScheduler().runTaskLater(this, ()-> {assistants.createNextRandomEvent();}, 5L);
        new Metrics(this, 23730);

    }

}
