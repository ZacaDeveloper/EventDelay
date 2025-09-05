package me.zaca.eventDelay.tools;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {

    private static final Pattern COLOR_PATTERN = Pattern.compile("<(#[a-fA-F0-9]{6})>(.*?)</(#[a-fA-F0-9]{6})>");

    public static List<String> hex(List<String> message) {
        List<String> list = new ArrayList<>();

        for (String string : message) {
            list.add(hex(string));
        }

        return list;
    }
    public static String hex(String message) {

        Matcher matcher = COLOR_PATTERN.matcher(message);
        StringBuilder resultBuffer = new StringBuilder();

        while (matcher.find()) {
            String startColor = matcher.group(1);  // Начальный HEX-код (#RRGGBB)
            String text = matcher.group(2);       // Текст между тегами
            String endColor = matcher.group(3);  // Конечный HEX-код (#RRGGBB)

            String gradientText = applyGradient(startColor, endColor, text);

            matcher.appendReplacement(resultBuffer, gradientText);
        }

        matcher.appendTail(resultBuffer);

        return ChatColor.translateAlternateColorCodes('&', resultBuffer.toString());
    }

    public static String hex(String message, Player player) {
        message = PlaceholderAPI.setPlaceholders(player, message);

        Matcher matcher = COLOR_PATTERN.matcher(message);
        StringBuilder resultBuffer = new StringBuilder();

        while (matcher.find()) {
            String startColor = matcher.group(1);  // Начальный HEX-код (#RRGGBB)
            String text = matcher.group(2);       // Текст между тегами
            String endColor = matcher.group(3);  // Конечный HEX-код (#RRGGBB)

            String gradientText = applyGradient(startColor, endColor, text);

            matcher.appendReplacement(resultBuffer, gradientText);
        }

        matcher.appendTail(resultBuffer);

        return ChatColor.translateAlternateColorCodes('&', resultBuffer.toString());
    }

    private static String applyGradient(String startColor, String endColor, String text) {
        int[] startRGB = hexToRgb(startColor);
        int[] endRGB = hexToRgb(endColor);

        StringBuilder result = new StringBuilder();
        String activeFormats = "";
        text = ChatColor.translateAlternateColorCodes('&', text); // сразу заменяем &l и т.д.

        int colorIndex = 0;
        int len = (int) text.chars().filter(c -> c != ChatColor.COLOR_CHAR).count(); // количество видимых символов

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ChatColor.COLOR_CHAR && i + 1 < text.length()) {
                char formatCode = text.charAt(i + 1);
                i++; // пропускаем код
                if (formatCode == 'r') {
                    activeFormats = ""; // сброс
                } else {
                    activeFormats += "" + ChatColor.COLOR_CHAR + formatCode;
                }
                continue;
            }

            double ratio = (double) colorIndex / Math.max(1, len - 1);
            int red = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * ratio);
            int green = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * ratio);
            int blue = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * ratio);

            String hexColor = String.format("§x§%s§%s§%s§%s§%s§%s",
                    Integer.toHexString((red >> 4) & 0xF),
                    Integer.toHexString(red & 0xF),
                    Integer.toHexString((green >> 4) & 0xF),
                    Integer.toHexString(green & 0xF),
                    Integer.toHexString((blue >> 4) & 0xF),
                    Integer.toHexString(blue & 0xF)
            );

            result.append(hexColor).append(activeFormats).append(c);
            colorIndex++;
        }

        return result.toString();
    }


    private static int[] hexToRgb(String hex) {
        return new int[]{
                Integer.valueOf(hex.substring(1, 3), 16), // Красный
                Integer.valueOf(hex.substring(3, 5), 16), // Зелёный
                Integer.valueOf(hex.substring(5, 7), 16)  // Синий
        };
    }

    public static String setPlaceholders(String text, Player player) {
        text = PlaceholderAPI.setPlaceholders(player, text);
        return text;
    }
    public static List<String> setPlaceholders(List<String> text, Player player) {
        text = PlaceholderAPI.setPlaceholders(player, text);
        return text;
    }

}
