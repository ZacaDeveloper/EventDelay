package me.zaca.eventDelay.manager;


import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.configurations.Messages;
import me.zaca.eventDelay.constructor.Events;
import me.zaca.eventDelay.constructor.Groups;
import me.zaca.eventDelay.tools.EventDelayAPI;
import me.zaca.eventDelay.tools.Logger;
import org.bukkit.Bukkit;

import java.util.*;

import static me.zaca.eventDelay.tools.Color.hex;


public class Assistants {

    private final Random random = new Random();

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;
    private final Messages messages;
    public Assistants(Main plugin, EventDelayAPI eventDelayAPI) {
        this.plugin = plugin;
        this.eventDelayAPI = eventDelayAPI;
        this.messages = plugin.getMessages();
    }

    public void createNextRandomEvent() {
        if (eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            String nextEvent;
            Map<String, Integer> eventChances = new HashMap<>();
            int totalWeight = 0;
            int onlinePlayers = Bukkit.getOnlinePlayers().size();

            for (String id : plugin.getCfg().getEvents().keySet()) {
                Events event = plugin.getCfg().getEvents().get(id);
                if (event.getMinPlayers() <= onlinePlayers) {
                    eventChances.put(id, event.getChance());
                    totalWeight += event.getChance();
                }
            }
            if (totalWeight > 0) {
                int randomValue = random.nextInt(totalWeight) + 1;
                int currentWeight = 0;

                for (Map.Entry<String, Integer> entry : eventChances.entrySet()) {
                    currentWeight += entry.getValue();
                    if (randomValue <= currentWeight) {
                        nextEvent = entry.getKey();
                        eventDelayAPI.setNextEvent(nextEvent);
                        return;
                    }
                }
            }
            List<Events> allEvents = new ArrayList<>();
            for (String id : plugin.getCfg().getEvents().keySet()) {
                allEvents.add(plugin.getCfg().getEvents().get(id));
            }

            if (allEvents.isEmpty()) {
                List<Events> list = new ArrayList<>(plugin.getCfg().getEvents().values());
                if (list.isEmpty()) {
                    Logger.error("[EventDelay] Не найдено ни одного события для выбора!");
                    return;
                }
                nextEvent = list.get(random.nextInt(list.size())).getId();
                eventDelayAPI.setNextEvent(nextEvent);
            }

        }

    }
    public void createRandomEventFromGroup(String groupId) {
        Groups group = plugin.getCfg().getGroups().get(groupId);
        if (group == null || group.events().isEmpty()) {
            createNextRandomEvent();
            return;
        }

        String randomEvent = group.events().get(random.nextInt(group.events().size()));
        eventDelayAPI.setNextEvent(randomEvent);
    }

    public void createRandomEventFromRandomGroup() {
        if (plugin.getCfg().getGroups().isEmpty()) {
            createNextRandomEvent();
            return;
        }

        int totalWeight = plugin.getCfg().getGroups().values().stream()
                .mapToInt(Groups::chance)
                .sum();

        if (totalWeight <= 0) {
            createNextRandomEvent();
            return;
        }

        int randomValue = random.nextInt(totalWeight) + 1;
        int currentWeight = 0;

        for (Groups group : plugin.getCfg().getGroups().values()) {
            currentWeight += group.chance();
            if (randomValue <= currentWeight) {
                createRandomEventFromGroup(group.id());
                return;
            }
        }

        createNextRandomEvent();
    }
    public boolean isEventActive() {
        return !eventDelayAPI.getNowEvent().equalsIgnoreCase("none");
    }

    public String getPreviousEventPrefix() {
        if (!eventDelayAPI.getPreviousEvent().equalsIgnoreCase("none")) {
            return hex(plugin.getCfg().getEvents().get(eventDelayAPI.getPreviousEvent()).getPrefix());
        }
        return "none";
    }

    public String getNowEventPrefix() {
        if (!eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return hex(plugin.getCfg().getEvents().get(eventDelayAPI.getNowEvent()).getPrefix());
        }
        return "none";
    }

    public String getNextEventPrefix() {
        if (!eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            return hex(plugin.getCfg().getEvents().get(eventDelayAPI.getNextEvent()).getPrefix());
        }
        return "none";
    }


    public boolean isCompass() {
        if (!eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return plugin.getCfg().getEvents().get(eventDelayAPI.getNowEvent()).isCompass();
        }
        return false;
    }

    public String activeStatus() {
        String check = eventDelayAPI.getActivationStatus();
        if (check.equals("true")) {
            return messages.getStart().replace("{time_to_open}", Integer.toString(eventDelayAPI.getOpeningTimer()));
        }
        if (check.equalsIgnoreCase("opened")) {
            return messages.getEnd();
        }
        return messages.getNone();
    }
}
