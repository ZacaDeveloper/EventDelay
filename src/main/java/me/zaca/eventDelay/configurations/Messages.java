package me.zaca.eventDelay.configurations;


import lombok.Getter;
import me.zaca.eventDelay.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

import static me.zaca.eventDelay.tools.Color.hex;

@Getter
public class Messages {

    private final Main plugin;
    public Messages(Main plugin) {
        this.plugin = plugin;
    }

    private List<String> time;
    private List<String> active;
    private List<String> noPlayers;
    private List<String> info;

    private String noPerm;
    private String tp_cooldown;
    private String reload;
    private List<String> usage;

    private String noItem;
    private String disabled;
    private String success;

    private String none;
    private String start;
    private String end;

    public void load(FileConfiguration configuration) {

        time = hex(configuration.getStringList("delay.time"));
        active = hex(configuration.getStringList("delay.active"));
        noPlayers = hex(configuration.getStringList("delay.noPlayers"));
        info = hex(configuration.getStringList("delay.info"));

        noItem = hex(configuration.getString("compass.noItem", "&c[✘] &fДля этой функции на руках должен быть компас."));
        disabled = hex(configuration.getString("compass.disabled", "&c[✘] &fКомпас отключён для этого ивента."));
        success = hex(configuration.getString("compass.success", "&a[✔] &fТеперь компас будет направлять на ивент."));

        none = hex(configuration.getString("OpeningTime.none", "&fЖдёт активации."));
        start = hex(configuration.getString("OpeningTime.start", "&fАктивация. До открытия &6{time_to_open}&f сек."));
        end = hex(configuration.getString("OpeningTime.end", "&aДоступ открыт."));


        reload = hex(configuration.getString("messages.reload"));
        noPerm = hex(configuration.getString("messages.noPerm"));
        tp_cooldown = hex(configuration.getString("messages.tp_cooldown"));
        usage = hex(configuration.getStringList("messages.usage"));
    }

}

