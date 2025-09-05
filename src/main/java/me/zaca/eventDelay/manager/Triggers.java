package me.zaca.eventDelay.manager;

import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.constructor.Events;
import me.zaca.eventDelay.tools.Actions;
import me.zaca.eventDelay.tools.EventDelayAPI;

import java.util.List;
import java.util.Random;

public class Triggers {

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;
    private final Assistants assistants;

    private final Random RANDOM = new Random();

    public Triggers(Main plugin) {
        this.plugin = plugin;
        this.eventDelayAPI = plugin.getEventDelayAPI();
        this.assistants = plugin.getAssistants();
    }

    public void startRandomEvent() {
        if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            eventDelayAPI.setNowEvent(eventDelayAPI.getNextEvent());
        } else {
            stopEvent(eventDelayAPI.getNowEvent());
            eventDelayAPI.setNowEvent(eventDelayAPI.getNextEvent());
        }

        assistants.createNextRandomEvent();
        triggerEvent(eventDelayAPI.getNowEvent());
    }

    public void startEvent(String eventName) {
        if (!eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            stopEvent(eventName);
        }
        eventDelayAPI.setNowEvent(eventName);
        eventDelayAPI.setNowEvent(eventName);
        assistants.createNextRandomEvent();
        triggerEvent(eventName);
    }


    public void stopEvent(String eventName) {
        if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return;
        }

        Events event = plugin.getCfg().getEvents().get(eventName);

        eventDelayAPI.setOpeningTimer(event.getDuration());

        Actions.execute(plugin, event.getOnEnd());

        eventDelayAPI.setPreviousEvent(eventDelayAPI.getNowEvent());
        eventDelayAPI.setNowEvent("none");
        assistants.createNextRandomEvent();

    }

    public void triggerNextEvent() {
        if (eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            String timerValue = plugin.getCfg().getTimeValue();

            if (timerValue != null && timerValue.contains(";")) {
                String eventSpec = timerValue.split(";")[1].trim();

                if (eventSpec.equals("$rand_group")) {
                    plugin.getAssistants().createRandomEventFromRandomGroup();
                } else if (plugin.getCfg().getGroups().containsKey(eventSpec)) {
                    plugin.getAssistants().createRandomEventFromGroup(eventSpec);
                } else if (plugin.getCfg().getEvents().containsKey(eventSpec)) {
                    eventDelayAPI.setNextEvent(eventSpec);
                } else {
                    plugin.getAssistants().createNextRandomEvent();
                }
            } else {
                plugin.getAssistants().createNextRandomEvent();
            }
        }

        eventDelayAPI.setNowEvent(eventDelayAPI.getNextEvent());
        eventDelayAPI.setNextEvent("none");
        eventDelayAPI.setActivationStatus("false");

        triggerEvent(eventDelayAPI.getNowEvent());

        if (plugin.getCfg().getTimerType().equalsIgnoreCase("DEFAULT")) {
            eventDelayAPI.setDelay(plugin.getCfg().getTimer());
        }
    }

    public void triggerEvent(String eventName) {
        Events event = plugin.getCfg().getEvents().get(eventName);

        eventDelayAPI.setOpeningTimer(event.getDuration());

        if (!event.getOnStartDefault().isEmpty()) {
            Actions.execute(plugin, event.getOnStartDefault());
        }
        if (!event.getOnStartRandom().isEmpty()) {
            int randomNumber = RANDOM.nextInt(event.getOnStartRandom().size());

            List<String> randomActions = event.getOnStartRandom().get(randomNumber);

            Actions.execute(plugin, randomActions);

    }

        plugin.getTimer().startDuration(eventDelayAPI.getNowEvent(), eventDelayAPI.getOpeningTimer());
    }

}
