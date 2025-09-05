package me.zaca.eventDelay.constructor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor @Getter
public class Events {
    final String id;
    final int duration;
    final int minPlayers;
    final String prefix;
    final int chance;
    final boolean compass;
    final String coordinatesWorld;
    final String coordinatesX;
    final String coordinatesY;
    final String coordinatesZ;
    final int activationTime;
    final List<String> activeInfo;
    final List<Integer> warnTimes;
    final List<String> warnActions;
    final List<String> onStartDefault;
    final Map<Integer, List<String>> onStartRandom;
    final List<String> onActivated;
    final List<String> onEnd;
}
