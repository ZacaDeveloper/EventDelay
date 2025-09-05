package me.zaca.eventDelay.constructor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class Webhook {
    final String id;
    final String url;
    final String avatar;
    final String username;
    final String color;
    final String title;
    final String text;
    final List<String> embedText;
}
