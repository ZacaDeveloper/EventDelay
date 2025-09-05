package me.zaca.eventDelay.configurations;


import lombok.Getter;
import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.constructor.Webhook;
import me.zaca.eventDelay.tools.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.zaca.eventDelay.tools.Color.setPlaceholders;
import static me.zaca.eventDelay.tools.FormatTimer.stringFormat;

@Getter
public class WebhookConfig {

    private final Main plugin;
    public WebhookConfig(Main plugin) {
        this.plugin = plugin;
    }
    private final Map<String, Webhook> webhooks = new HashMap<>();

    public void load(FileConfiguration configuration) {
        webhooks.clear();
        ConfigurationSection webhooksSection = configuration.getConfigurationSection("webhooks");
        for (String id : webhooksSection.getKeys(false)) {

            ConfigurationSection section = webhooksSection.getConfigurationSection(id);

            if (section==null) {
                Logger.error("Секция 'Events."+id+"' не найдена");
                continue;
            }

            Webhook webhook = new Webhook(
                    id,
                    section.getString("Url"),
                    section.getString("Avatar"),
                    section.getString("Username"),
                    section.getString("color"),
                    section.getString("title"),
                    section.getString("text", ""),
                    section.getStringList("embedText"));

            webhooks.put(id, webhook);
        }
    }


    public void sendToDiscord(Webhook webhook) {
        try {
            URL url = new URL(webhook.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            int decimalColor = Integer.parseInt(webhook.getColor().replace("#", ""), 16);

            StringBuilder descriptionBuilder = new StringBuilder();
            for (String text : webhook.getEmbedText()) {
                descriptionBuilder.append(text).append("\\n");
            }


            String title = setPlaceholders(webhook.getTitle().replace("{prefix}", plugin.getAssistants().getNowEventPrefix())
                    .replace("{duration}", String.valueOf(plugin.getEventDelayAPI().getDuration()))
                    .replace("{duration_string}", stringFormat(plugin.getEventDelayAPI().getDuration()))
                    .replace("{active_status}", plugin.getAssistants().activeStatus()), null);

            String text = setPlaceholders(webhook.getText().replace("{prefix}", plugin.getAssistants().getNowEventPrefix())
                    .replace("{duration}", String.valueOf(plugin.getEventDelayAPI().getDuration()))
                    .replace("{duration_string}", stringFormat(plugin.getEventDelayAPI().getDuration()))
                    .replace("{active_status}", plugin.getAssistants().activeStatus()), null);

            String json = """
                {
                  "username": "%s",
                  "avatar_url": "%s",
                  "content": "%s",
                  "embeds": [{
                    "title": "%s",
                    "description": "%s",
                    "color": %d
                  }]
                }
                """.formatted(webhook.getUsername(),
                    webhook.getAvatar(),
                    text,
                    title,
                    descriptionBuilder.toString(),
                    decimalColor);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                Logger.warn("Ошибка отправки webhook: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

