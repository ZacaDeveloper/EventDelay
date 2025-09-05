package me.zaca.eventDelay.tools;

import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.constructor.Events;
import me.zaca.eventDelay.constructor.Webhook;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static me.zaca.eventDelay.tools.Color.hex;
import static me.zaca.eventDelay.tools.Color.setPlaceholders;

public class Actions {

    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    private static int teleportRadius = 10;
    private static int teleportCooldown = 15;

    public static void execute(Main plugin,List<String> commands) {
        executeWithDelay(plugin, commands, 0);
    }

    private static void executeWithDelay(Main plugin,List<String> commands, int index) {
        if (index >= commands.size()) return;

        String command = commands.get(index);
        String[] args = command.split(" ");
        String withoutCMD = command.replace(args[0] + " ", "");

        if (args[0].equalsIgnoreCase("[DELAY]")) {
            int delayTicks = Integer.parseInt(args[1]);
            Bukkit.getScheduler().runTaskLater(plugin, () -> executeWithDelay(plugin, commands, index + 1), delayTicks);
            return;
        }

        if (args[0].startsWith("[TELEPORT_BUTTON=")) {
            int radius;
            try {
                String radiusStr = args[0].substring(args[0].indexOf("=") + 1, args[0].indexOf("]"));
                radius = Integer.parseInt(radiusStr);
            } catch (Exception e) {
                radius = 10;
            }
            teleportRadius = radius;

            int cooldownSeconds = 0;
            if (command.contains("--cooldown:")) {
                try {
                    cooldownSeconds = Integer.parseInt(command.split("--cooldown:")[1].split(" ")[0]);
                } catch (Exception ignored) {
                }
            }
            String[] buttonParams = withoutCMD.split(";", 2);
            if (buttonParams.length < 2) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(hex("&cОшибка формата: используйте [TELEPORT_BUTTON=радиус] текст;подсказка"));
                }
                return;
            }
            teleportCooldown = cooldownSeconds;

            TextComponent msg = new TextComponent(hex(buttonParams[0].replace("--cooldown:" + cooldownSeconds, "").trim()));
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event teleport"));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hex(buttonParams[1].replace("--cooldown:" + cooldownSeconds, "").trim()))));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(ChatMessageType.CHAT, msg);
            }
            executeWithDelay(plugin,commands, index + 1);
            return;
        }

        if (args[0].startsWith("[TELEPORT_NEAR=")) {
            int radius = 10;
            try {
                radius = Integer.parseInt(args[0].substring(args[0].indexOf("=") + 1, args[0].indexOf("]")));
            } catch (Exception ignored) {
            }

            String[] params = withoutCMD.split(";");
            if (params.length < 4) return;

            World world = Bukkit.getWorld(params[0].trim());
            if (world == null) return;
            try {
                Location center = new Location(
                        world,
                        Double.parseDouble(params[1].trim()),
                        Double.parseDouble(params[2].trim()),
                        Double.parseDouble(params[3].trim())
                );
                for (Player player : Bukkit.getOnlinePlayers()) {
                    teleportNear(plugin, player, center, radius, 0);
                }
            } catch (NumberFormatException ignored) {
            }
            executeWithDelay(plugin,commands, index + 1);
            return;
        }

        switch (args[0].toUpperCase()) {
            case "[MESSAGE]", "[MSG]", "[MESSAGE_ALL]": {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(hex(withoutCMD, player));
                }
                break;
            }
            case "[TELEPORT]", "[TP]": {
                String[] parts = withoutCMD.split(" ");
                if (parts.length == 4) {
                    try {
                        String worldName = parts[0];
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);

                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            Bukkit.getLogger().warning("Мир " + worldName + " не найден");
                            break;
                        }

                        Location location = new Location(world, x, y, z);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.teleport(location);
                        }

                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("Ошибка парсинга координат");
                        break;
                    }
                }
                if (parts.length >= 6) {
                    try {
                        String worldName = parts[0];
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        float yaw = Float.parseFloat(parts[4]);
                        float pitch = Float.parseFloat(parts[5]);

                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            Bukkit.getLogger().warning("Мир " + worldName + " не найден");
                            break;
                        }

                        Location location = new Location(world, x, y, z, yaw, pitch);

                        Bukkit.getScheduler().runTask(plugin, ()-> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.teleport(location);
                            }
                        });

                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("Ошибка парсинга координат");
                        break;
                    }
                } else {
                    Bukkit.getLogger().warning("Некорректные данные для телепорта");
                    break;
                }
                break;
            }
            case "[PLAYER]": {
                String finalWithoutCMD = withoutCMD;
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.chat("/"+finalWithoutCMD.replace("%player%", player.getName()));
                    }
                });

                break;
            }
            case "[CONSOLE]": {
                String finalWithoutCMD = withoutCMD;
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hex(finalWithoutCMD));
                });
                break;
            }
            case "[SEND_WEBHOOK]": {
                String finalWithoutCMD = withoutCMD;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
                    Webhook webhook = plugin.getWebhookConfig().getWebhooks().get(finalWithoutCMD);
                    plugin.getWebhookConfig().sendToDiscord(webhook);
                });
                break;
            }

            case "[ACTIONBAR]": {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(hex(withoutCMD
                            .replace("%player%", player.getName()), player)));
                }
                break;
            }
            case "[SOUND]": {
                float volume = 1.0f;
                float pitch = 1.0f;
                for (String arg : args) {
                    if (arg.startsWith("-volume:")) {
                        volume = Float.parseFloat(arg.replace("-volume:", ""));
                        continue;
                    }
                    if (!arg.startsWith("-pitch:")) continue;
                    pitch = Float.parseFloat(arg.replace("-pitch:", ""));
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.valueOf(args[1]), volume, pitch);
                }
                break;
            }
            case "[EFFECT]": {
                int strength = 0;
                int duration = 1;
                for (String arg : args) {
                    if (arg.startsWith("-strength:")) {
                        strength = Integer.parseInt(arg.replace("-strength:", ""));
                        continue;
                    }
                    if (!arg.startsWith("-duration:")) continue;
                    duration = Integer.parseInt(arg.replace("-duration:", ""));
                }
                PotionEffectType effectType = PotionEffectType.getByName(args[1]);
                if (effectType == null) {
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPotionEffect(effectType)) {
                        continue;
                    }
                    player.addPotionEffect(new PotionEffect(effectType, duration * 20, strength));
                }
                break;
            }
            case "[TITLE]": {
                String title = "";
                String subTitle = "";
                int fadeIn = 1;
                int stay = 3;
                int fadeOut = 1;
                for (String arg : args) {
                    if (arg.startsWith("-fadeIn:")) {
                        fadeIn = Integer.parseInt(arg.replace("-fadeIn:", ""));
                        withoutCMD = withoutCMD.replace(arg, "");
                        continue;
                    }
                    if (arg.startsWith("-stay:")) {
                        stay = Integer.parseInt(arg.replace("-stay:", ""));
                        withoutCMD = withoutCMD.replace(arg, "");
                        continue;
                    }
                    if (!arg.startsWith("-fadeOut:")) continue;
                    fadeOut = Integer.parseInt(arg.replace("-fadeOut:", ""));
                    withoutCMD = withoutCMD.replace(arg, "");
                }
                String[] message = hex(withoutCMD).split(";");
                if (message.length >= 1) {
                    title = message[0];
                    if (message.length >= 2) {
                        subTitle = message[1];
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(title, subTitle, fadeIn * 20, stay * 20, fadeOut * 20);
                }

            }
        }
        executeWithDelay(plugin,commands, index + 1);
    }

    private static final Random random = new Random();

    public static void teleportNear(Main plugin, Player player, Location center, int radius, int cooldownSeconds) {
        if (cooldownSeconds > 0) {
            Long lastTeleport = teleportCooldowns.get(player.getUniqueId());
            if (lastTeleport != null && (System.currentTimeMillis() - lastTeleport) < cooldownSeconds * 1000L) {
                int remaining = (int) (cooldownSeconds - (System.currentTimeMillis() - lastTeleport) / 1000);
                player.sendMessage(setPlaceholders(plugin.getMessages().getTp_cooldown()
                        .replace("{time}", String.valueOf(remaining)), player));
                return;
            }
        }

        double angle = random.nextDouble() * 2 * Math.PI;
        double newX = center.getX() + radius * Math.cos(angle);
        double newZ = center.getZ() + radius * Math.sin(angle);

        World world = center.getWorld();
        double newY = world.getHighestBlockYAt((int) newX, (int) newZ) + 1;

        Location newLocation = new Location(world, newX, newY, newZ);
        Bukkit.getScheduler().runTask(plugin, ()-> {
            player.teleport(newLocation);
        });


        if (cooldownSeconds > 0) {
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public static void teleportButton(Main plugin, Player player, EventDelayAPI eventDelayAPI) {
        if (!plugin.getCfg().getEvents().get(eventDelayAPI.getNowEvent()).isCompass()) {
            Logger.error("Кнопка телепорта не будет работать так как у вас отключён компас или отсутствуют корректные координаты в coordinates");
            return;
        }

        Events event = plugin.getCfg().getEvents().get(eventDelayAPI.getNowEvent());

        World world = Bukkit.getWorld(event.getCoordinatesWorld());

        String xString = hex(event.getCoordinatesX(), player);
        String yString = hex(event.getCoordinatesY(), player);
        String zString = hex(event.getCoordinatesZ(), player);

        int x = Integer.parseInt(xString);
        int y = Integer.parseInt(yString);
        int z = Integer.parseInt(zString);

        Location center = new Location(world, x, y, z);

        if (teleportCooldown > 0) {
            Long lastTeleport = teleportCooldowns.get(player.getUniqueId());
            if (lastTeleport != null && (System.currentTimeMillis() - lastTeleport) < teleportCooldown * 1000L) {
                int remaining = (int) (teleportCooldown - (System.currentTimeMillis() - lastTeleport) / 1000);
                player.sendMessage(setPlaceholders(plugin.getMessages().getTp_cooldown()
                        .replace("{time}", String.valueOf(remaining)), player));
                return;
            }
        }

        double angle = random.nextDouble() * 2 * Math.PI;
        double newX = center.getX() + teleportRadius * Math.cos(angle);
        double newZ = center.getZ() + teleportRadius * Math.sin(angle);

        double newY = world.getHighestBlockYAt((int) newX, (int) newZ) + 1;

        Location newLocation = new Location(world, newX, newY, newZ);
        Bukkit.getScheduler().runTask(plugin, ()-> {
            player.teleport(newLocation);
        });

        if (teleportCooldown > 0) {
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
}
