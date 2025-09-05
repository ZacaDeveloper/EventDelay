package me.zaca.eventDelay.tools;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDelayAPI {
    private boolean freeze;
    private int timer;
    private int minPlayers;
    private String previousEvent;
    private String nowEvent;
    private String nextEvent;
    private String ActivationStatus;
    private int delay;
    private int duration;
    private int timeUntilDuration;
    private int openingTimer;

    public EventDelayAPI(boolean freeze,
                         int timer,
                         int minPlayers,
                         String previousEvent,
                         String nowEvent,
                         String nextEvent) {
        this.freeze = freeze;
        this.timer = timer;
        this.minPlayers = minPlayers;
        this.previousEvent = previousEvent;
        this.nowEvent = nowEvent;
        this.nextEvent = nextEvent;
    }
}