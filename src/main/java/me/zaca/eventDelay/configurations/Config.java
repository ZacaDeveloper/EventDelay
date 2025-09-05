package me.zaca.eventDelay.configurations;

import lombok.Getter;
import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.constructor.Events;
import me.zaca.eventDelay.constructor.Groups;
import me.zaca.eventDelay.tools.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Config {

    private final Main plugin;
    private final FileConfiguration configuration;

    private String license;
    private boolean debug;
    private int timer;
    private String timerType;
    private String timerZone;
    private String timeValue;
    private List<String> timerZones;
    private boolean freeze;
    private int minPlayers;
    private final Map<String, Events> events = new HashMap<>();
    private final Map<String, Groups> groups = new HashMap<>();

    public Config(Main plugin) {
        this.plugin = plugin;
        this.configuration = getFileConfiguration(plugin.getDataFolder().getAbsolutePath(), "config.yml");


    }

    public void load() {
        license = configuration.getString("license.key", "NONE");
        debug = configuration.getBoolean("debug", false);

        timeValue = configuration.getString("AutoStart.time", "1800");
        if (timeValue.contains(";")) {
            String[] parts = timeValue.split(";");
            timer = Integer.parseInt(parts[0].trim());
        } else {
            timer = Integer.parseInt(timeValue);
        }
        timerType = configuration.getString("AutoStart.type", "TIMER");
        timerZone = configuration.getString("AutoStart.zone", "GMT+3");
        timerZones = configuration.getStringList("AutoStart.times");
        freeze = configuration.getBoolean("AutoStart.Freeze", false);
        minPlayers = configuration.getInt("AutoStart.minPlayers", 1);

        loadEvents();
        loadGroups();
    }

    public void save(String configPath, String value) {
        try {
            File file = new File(plugin.getDataFolder(), "config.yml");
            if (!file.exists()) {
                plugin.saveResource("config.yml", false);
            }
            configuration.set(configPath, value);
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadGroups() {
        ConfigurationSection configurationSection = configuration.getConfigurationSection("Groups");
        groups.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
           for (String id : configurationSection.getKeys(false)) {
               ConfigurationSection section = configurationSection.getConfigurationSection(id);
               if (section==null) {
                   Logger.error("Секция 'Groups."+id+"' не найдена");
                   continue;
               }

               int chance = section.getInt("Chance", 100);
               List<String> events = section.getStringList("Events");

               Groups group = new Groups(id, chance, events);
               groups.put(id, group);
           }
        });
    }

    private void loadEvents() {
        ConfigurationSection configurationSection = configuration.getConfigurationSection("Events");
        events.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
            for (String id : configurationSection.getKeys(false)) {

                ConfigurationSection section = configurationSection.getConfigurationSection(id);

                if (section==null) {
                    Logger.error("Секция 'Events."+id+"' не найдена");
                    continue;
                }

                int duration = section.getInt("Duration", 300);
                int chance = section.getInt("Chance", 100);
                int eventMinPlayers = section.getInt("MinPlayers", 0);
                String prefix = section.getString("Prefix", "");
                boolean compass = section.getBoolean("compass", false);

                String coordinatesWorld = "world";
                String coordinatesX = "none";
                String coordinatesY = "none";
                String coordinatesZ = "none";

                if (compass) {
                    coordinatesWorld = section.getString("coordinates.world");
                    coordinatesX = section.getString("coordinates.x");
                    coordinatesY = section.getString("coordinates.y");
                    coordinatesZ = section.getString("coordinates.z");
                }

                if (coordinatesWorld==null || Bukkit.getWorld(coordinatesWorld)==null) {
                    if (debug) {
                        Logger.warn(id+": Мир для компаса не был найден.");
                    }
                    compass = false;
                }
                if (coordinatesX==null || coordinatesX.equalsIgnoreCase("none")) {
                    if (debug) {
                        Logger.warn(id+": Координат X для компаса не был найден.");
                    }
                    compass = false;
                }
                if (coordinatesY==null || coordinatesY.equalsIgnoreCase("none")) {
                    if (debug) {
                        Logger.warn(id+": Координат Y для компаса не был найден.");
                    }
                    compass = false;
                }
                if (coordinatesZ==null || coordinatesZ.equalsIgnoreCase("none")) {
                    if (debug) {
                        Logger.warn(id+": Координат Z для компаса не был найден.");
                    }
                    compass = false;
                }

                int activationTime = section.getInt("ActivationTime", 0);
                List<String> activeInfo = section.getStringList("activeInfo");

                List<Integer> warnTimes = new ArrayList<>(section.getIntegerList("warns.time"));
                List<String> warnActions = new ArrayList<>(section.getStringList("warns.warnActions"));

                List<String> onStartDefault = section.getStringList("onStart.default");
                Map<Integer, List<String>> onStartRandom = new HashMap<>();

                ConfigurationSection randomSection = section.getConfigurationSection("onStart.random");
                if (randomSection != null) {
                    int i = 0;
                    for (String string : randomSection.getKeys(false)) {
                        onStartRandom.put(i, section.getStringList("onStart.random." + string));
                        i++;
                    }
                }


                List<String> onActivated = section.getStringList("onActivated");
                List<String> onEnd = section.getStringList("onEnd");

                Events event = new Events(
                        id,
                        duration,
                        eventMinPlayers,
                        prefix,
                        chance,
                        compass,
                        coordinatesWorld,
                        coordinatesX,
                        coordinatesY,
                        coordinatesZ,
                        activationTime,
                        activeInfo,
                        warnTimes,
                        warnActions,
                        onStartDefault,
                        onStartRandom,
                        onActivated,
                        onEnd
                );
                events.put(id, event);
            }
        });

    }


    public FileConfiguration getFileConfiguration(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }


    public File getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return file;
    }
}
