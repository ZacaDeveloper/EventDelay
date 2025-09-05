package me.zaca.eventDelay.tools;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.manager.Assistants;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventDelayExpansion extends PlaceholderExpansion {
    
    private final Main plugin;
    
    public EventDelayExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "eventdelay";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        EventDelayAPI eventDelayAPI = plugin.getEventDelayAPI();

        return switch (identifier.toLowerCase()) {
            case "now" -> eventDelayAPI.getNowEvent();
            case "next" -> eventDelayAPI.getNextEvent();
            case "previous" -> eventDelayAPI.getPreviousEvent();
            case "previous_prefix" -> plugin.getAssistants().getPreviousEventPrefix();
            case "time_to_start" -> Integer.toString(eventDelayAPI.getDelay());
            case "time_to_start_string" -> FormatTimer.stringFormat(eventDelayAPI.getDelay());
            case "duration" -> String.valueOf(eventDelayAPI.getOpeningTimer());
            case "duration_string" -> FormatTimer.stringFormat(eventDelayAPI.getOpeningTimer());
            case "prefix" -> plugin.getAssistants().getNowEventPrefix();
            case "prefix_next", "next_prefix" -> plugin.getAssistants().getNextEventPrefix();
            default -> null;
        };
    }

}
