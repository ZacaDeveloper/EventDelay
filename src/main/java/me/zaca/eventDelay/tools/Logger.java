package me.zaca.eventDelay.tools;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Logger {

    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§e[EventDelay] "+ message);
    }
    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§a[EventDelay] §f"+ message);
    }
    public void success(String message) {
        Bukkit.getConsoleSender().sendMessage("§a[EventDelay] §a"+ message);
    }
    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§c[EventDelay] "+ message);
    }
    public void msg(String message) {
        Bukkit.getConsoleSender().sendMessage("§6[EventDelay] §f"+ message);
    }
}