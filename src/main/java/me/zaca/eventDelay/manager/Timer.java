package me.zaca.eventDelay.manager;

import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.tools.Actions;
import me.zaca.eventDelay.tools.EventDelayAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Timer {

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;
    private final Triggers triggers;

    private BukkitRunnable startingTimer;
    private BukkitRunnable activationTimer;
    private BukkitRunnable durationTimer;

    public Timer(Main plugin) {
        this.plugin = plugin;
        this.eventDelayAPI = plugin.getEventDelayAPI();
        this.triggers = plugin.getTriggers();
    }

    private static final Map<String, String> timezoneEvents = new HashMap<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static ZoneId zoneId;
    private static long nextEventTime = 0;

    public void initialize() {
        String timezone = plugin.getCfg().getTimerZone();
        zoneId = ZoneId.of(timezone);
        if (plugin.getCfg().getTimerType().equalsIgnoreCase("TIMEZONE")) {
            for (String timeEntry : plugin.getCfg().getTimerZones()) {
                String[] parts = timeEntry.split(";");
                String time = parts[0].trim();
                String event = parts.length > 1 ? parts[1].trim() : null;
                timezoneEvents.put(time, event);
            }
        }
    }

    public void startTimer() {
        String timerType = plugin.getCfg().getTimerType();
        int timer = plugin.getCfg().getTimer();
        if (timerType.equalsIgnoreCase("TIMEZONE")) {
            startTimezoneTimer();
        } else {
            startDefaultTimer(timer);
        }
    }

    private void startDefaultTimer(int timer) {
        if (startingTimer != null) {
            startingTimer.cancel();
        }
        eventDelayAPI.setDelay(timer);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getDelay() > 0) {
                    if (eventDelayAPI.isFreeze()) {
                        if (Bukkit.getOnlinePlayers().size() >= eventDelayAPI.getMinPlayers()) {
                            eventDelayAPI.setDelay(eventDelayAPI.getDelay() - 1);
                        }
                    } else {
                        eventDelayAPI.setDelay(eventDelayAPI.getDelay() - 1);
                    }
                    checkWarnings();
                } else {
                    if (!plugin.getAssistants().isEventActive()) {
                        triggers.triggerNextEvent();
                    }
                    eventDelayAPI.setDelay(timer);
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
        startingTimer = task;
    }

    private void checkWarnings() {
        if (!eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            List<Integer> warnTimes = plugin.getCfg().getEvents().get(eventDelayAPI.getNextEvent()).getWarnTimes();
            List<String> warnActions = plugin.getCfg().getEvents().get(eventDelayAPI.getNextEvent()).getWarnActions();
            warnActions.replaceAll(s -> s
                    .replace("{prefix}", plugin.getAssistants().getNextEventPrefix())
                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getDelay()))
            );
            if (warnTimes.contains(eventDelayAPI.getDelay())) {
                Actions.execute(plugin, warnActions);
            }
        }
    }

    private void startTimezoneTimer() {
        if (startingTimer != null) {
            startingTimer.cancel();
        }

        startingTimer = new BukkitRunnable() {
            @Override
            public void run() {
                calculateNextEventTime();
                long nowMillis = System.currentTimeMillis();
                long remainingMillis = (nextEventTime * 1000L) - nowMillis;
                int remainingSeconds = (int) (remainingMillis / 1000);

                eventDelayAPI.setDelay(remainingSeconds);

                if (remainingSeconds <= 0) {
                    if (Bukkit.getOnlinePlayers().size() >= eventDelayAPI.getMinPlayers()) {
                        if (!plugin.getAssistants().isEventActive()) {
                            String currentTimeStr = LocalTime.now(zoneId).format(timeFormatter);
                            String eventSpec = timezoneEvents.get(currentTimeStr);

                            if (eventSpec != null && !eventSpec.isEmpty()) {
                                if (eventSpec.startsWith("$rand_group")) {
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

                            triggers.triggerNextEvent();
                        }
                    }
                    calculateNextEventTime();
                } else {
                    checkWarnings();
                }
            }
        };
        startingTimer.runTaskTimerAsynchronously(plugin, 0, 20L);
    }


    private void calculateNextEventTime() {
        long now = System.currentTimeMillis() / 1000;
        nextEventTime = Long.MAX_VALUE;

        for (String timeStr : timezoneEvents.keySet()) {
            long eventTime = parseTimeString(timeStr);
            if (eventTime > now && eventTime < nextEventTime) {
                nextEventTime = eventTime;
            }
        }

        if (nextEventTime == Long.MAX_VALUE && !timezoneEvents.isEmpty()) {
            String firstTime = timezoneEvents.keySet().iterator().next();
            nextEventTime = parseTimeString(firstTime) + 86400;
        }
    }

    private long parseTimeString(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length < 2) {
            return Long.MAX_VALUE;
        }
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next = now.withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .truncatedTo(ChronoUnit.SECONDS);

        if (next.isBefore(now)) {
            next = next.plusDays(1);
        }

        return next.toEpochSecond();
    }

    public void startDuration(String eventName, int duration) {
        if (durationTimer != null) {
            durationTimer.cancel();
        }
        eventDelayAPI.setDuration(duration);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getDuration() > 0) {
                    eventDelayAPI.setDuration(eventDelayAPI.getDuration() - 1);
                } else if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                    cancel();
                } else {
                    List<String> commands = plugin.getCfg().getEvents().get(eventName).getOnEnd();
                    Actions.execute(plugin, commands);
                    eventDelayAPI.setPreviousEvent(eventDelayAPI.getNowEvent());
                    eventDelayAPI.setNowEvent("none");
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
        durationTimer = task;
    }

    public void Activate() {
        if (activationTimer != null) {
            activationTimer.cancel();
        }
        eventDelayAPI.setOpeningTimer(plugin.getCfg().getEvents().get(eventDelayAPI.getNowEvent()).getActivationTime());
        eventDelayAPI.setActivationStatus("true");
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getOpeningTimer() > 0) {
                    eventDelayAPI.setOpeningTimer(eventDelayAPI.getOpeningTimer() - 1);
                } else {
                    List<String> onActivated = plugin.getCfg().getEvents().get(eventDelayAPI.getNowEvent()).getOnActivated();
                    Actions.execute(plugin, onActivated);
                    eventDelayAPI.setActivationStatus("opened");
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
        activationTimer = task;
    }
}