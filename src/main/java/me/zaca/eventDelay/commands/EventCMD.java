package me.zaca.eventDelay.commands;

import me.zaca.eventDelay.Main;
import me.zaca.eventDelay.configurations.Config;
import me.zaca.eventDelay.configurations.Messages;
import me.zaca.eventDelay.manager.Assistants;
import me.zaca.eventDelay.manager.Timer;
import me.zaca.eventDelay.manager.Triggers;
import me.zaca.eventDelay.tools.EventDelayAPI;
import me.zaca.eventDelay.tools.Logger;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.zaca.eventDelay.tools.Actions.teleportButton;
import static me.zaca.eventDelay.tools.Color.hex;
import static me.zaca.eventDelay.tools.Color.setPlaceholders;
import static me.zaca.eventDelay.tools.FormatTimer.stringFormat;

public class EventCMD implements TabExecutor {

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;
    private final Timer timer;
    private final Triggers triggers;
    private final Messages messages;
    private final Config config;
    private final Assistants assistants;

    public EventCMD(Main plugin) {
        this.plugin = plugin;
        this.eventDelayAPI = plugin.getEventDelayAPI();
        this.timer = plugin.getTimer();
        this.triggers = plugin.getTriggers();
        this.messages = plugin.getMessages();
        this.assistants = plugin.getAssistants();
        this.config = plugin.getCfg();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {


        if (sender instanceof Player p) {
            if (args.length == 0) {
                for (String msg : messages.getUsage()) {
                    sender.sendMessage(hex(msg, p));
                }
                return true;
            }
            String arg = args[0];
            switch (arg) {
                case "delay": {
                    if (Bukkit.getOnlinePlayers().size() < config.getMinPlayers()) {
                        for (String m : messages.getNoPlayers()) {
                            sender.sendMessage(m
                                    .replace("{min_players}", String.valueOf(config.getMinPlayers()))
                            );
                        }
                        return true;
                    }
                    if (assistants.isEventActive()) {
                        for (String m : messages.getActive()) {
                            m = m
                                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getDelay()))
                                    .replace("{time_to_start_string}", stringFormat(eventDelayAPI.getDelay()))
                                    .replace("{prefix}", assistants.getNowEventPrefix())
                            ;
                            sender.sendMessage(setPlaceholders(m, p));
                        }
                        return true;
                    } else {
                        for (String m : messages.getTime()) {
                            sender.sendMessage(setPlaceholders(m
                                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getDelay()))
                                    .replace("{time_to_start_string}", stringFormat(eventDelayAPI.getDelay())),p
                            ));
                        }
                    }
                    break;
                }
                case "info": {
                    if (assistants.isEventActive()) {

                        List<String> msg = hex(config.getEvents().get(eventDelayAPI.getNowEvent()).getActiveInfo());

                        for (String m : msg) {
                            m = (m
                                    .replace("{prefix}", assistants.getNowEventPrefix())
                                    .replace("{duration}", String.valueOf(eventDelayAPI.getDuration()))
                                    .replace("{duration_string}", stringFormat(eventDelayAPI.getDuration()))
                                    .replace("{active_status}", assistants.activeStatus())
                            );
                            p.sendMessage(setPlaceholders(m, p));
                        }

                    } else {
                        for (String msg : messages.getInfo()) {
                            p.sendMessage(msg);
                        }
                    }
                    break;
                }
                case "start": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(messages.getNoPerm());
                        break;
                    }

                    if (args.length==1) {
                        eventDelayAPI.setActivationStatus("false");
                        triggers.startRandomEvent();
                        eventDelayAPI.setDelay(config.getTimer());
                    } else if (args.length==2) {
                        eventDelayAPI.setActivationStatus("false");
                        triggers.startEvent(args[1]);
                        eventDelayAPI.setDelay(config.getTimer());
                    }
                    break;

                }
                case "teleport": {
                    if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                        break;
                    }

                    teleportButton(plugin, p, eventDelayAPI);
                    break;
                }
                case "stop": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    if (assistants.isEventActive()) {
                        eventDelayAPI.setOpeningTimer(0);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + assistants.getNowEventPrefix() + " остановлен."));
                        triggers.stopEvent(eventDelayAPI.getNowEvent());


                    } else {
                        sender.sendMessage("Нету активных ивентов");
                    }
                    break;
                }
                case "timer": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(messages.getNoPerm());
                        return true;
                    }

                    if (args.length < 3) {

                        p.sendMessage(hex("&a[HELP] &fСбросить таймер &c/event timer reset &7<duration/activation/delay>", p));
                        p.sendMessage(hex("&a[HELP] &fПоставить значение &c/event timer set &7<duration/activation/delay> <в секундах>", p));

                        return true;
                    }
                    if (args[1].equalsIgnoreCase("reset")) {
                        if (args[2].equalsIgnoreCase("duration")) {

                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(config.getEvents().get(eventDelayAPI.getNowEvent()).getDuration());
                                sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                            } else {
                                sender.sendMessage(hex("&cНету активного ивента."));
                            }


                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(config.getEvents().get(eventDelayAPI.getNowEvent()).getActivationTime());
                                sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                            } else {
                                sender.sendMessage(hex("&cНету активного ивента."));
                            }

                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(config.getTimer());
                            sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }
                        p.sendMessage("Вы успешно сбросили время до начала ивента");
                        break;
                    }
                    if (args[1].equalsIgnoreCase("set") && args.length == 4) {
                        if (args[2].equalsIgnoreCase("duration")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("&a[HELP] &fУ таймера &e" + args[2] + " &fуспешно установлено значение &a" + args[3] + "&f."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }
                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("&a[HELP] &fУ таймера &e"+args[2]+" &fуспешно установлено значение &a"+args[3]+"&f."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }

                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(Integer.parseInt(args[3]));
                            sender.sendMessage(hex("&a[HELP] &fУ таймера &e"+args[2]+" &fуспешно установлено значение &a"+args[3]+"&f."));
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }

                    }

                    break;
                }
                case "setNext": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("Используйте /event setNext <название ивента>");
                        return true;
                    }

                    if (config.getEvents().containsKey(args[1])) {
                        eventDelayAPI.setNextEvent(args[1]);
                        sender.sendMessage("Следующий ивент установлен на: " + ChatColor.GREEN + eventDelayAPI.getNextEvent());
                    } else {
                        sender.sendMessage("Ивент с названием " + ChatColor.RED + args[1] + ChatColor.WHITE + " не найден.");
                    }
                    break;
                }
                case "reload": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    p.sendMessage(messages.getReload());

                    plugin.setCfg(new Config(plugin));
                    config.load();
                    final FileConfiguration messagesFile = config.getFileConfiguration(plugin.getDataFolder().getAbsolutePath(), "messages.yml");
                    final FileConfiguration webhookFile = config.getFileConfiguration(plugin.getDataFolder().getAbsolutePath(), "webhook.yml");

                    plugin.getMessages().load(messagesFile);
                    plugin.getWebhookConfig().load(webhookFile);


                    eventDelayAPI.setOpeningTimer(config.getTimer());

                    eventDelayAPI.setTimer(config.getTimer());
                    eventDelayAPI.setFreeze(config.isFreeze());
                    eventDelayAPI.setMinPlayers(config.getMinPlayers());

                    eventDelayAPI.setPreviousEvent("none");
                    eventDelayAPI.setNowEvent("none");
                    eventDelayAPI.setNextEvent("none");

                    timer.initialize();
                    timer.startTimer();
                    assistants.createNextRandomEvent();

                    break;
                }
                case "activate": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    if (config.getEvents().containsKey(eventDelayAPI.getNowEvent())) {
                        if (assistants.isEventActive()) {
                            timer.Activate();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + assistants.getNowEventPrefix() + " активирован."));
                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                    }
                    break;
                }
                case "compass": {
                    if (assistants.isEventActive()) {
                        if (assistants.isCompass()) {

                            World world = Bukkit.getWorld(config.getEvents().get(eventDelayAPI.getNowEvent()).getCoordinatesWorld());

                            String xString = hex(config.getEvents().get(eventDelayAPI.getNowEvent()).getCoordinatesX(), p);
                            String yString = hex(config.getEvents().get(eventDelayAPI.getNowEvent()).getCoordinatesY(), p);
                            String zString = hex(config.getEvents().get(eventDelayAPI.getNowEvent()).getCoordinatesZ(), p);

                            int x = Integer.parseInt(xString);
                            int y = Integer.parseInt(yString);
                            int z = Integer.parseInt(zString);

                            ItemStack itemInHand = p.getInventory().getItemInMainHand();

                            if (itemInHand.getType() == Material.COMPASS) {

                                Location targetLocation = new Location(world, x, y, z);
                                p.setCompassTarget(targetLocation);
                                p.sendMessage(setPlaceholders(messages.getSuccess(), p));

                            } else {
                                p.sendMessage(setPlaceholders(messages.getNoItem(), p));
                            }

                        } else {
                            p.sendMessage(setPlaceholders(messages.getDisabled(), p));
                        }
                    } else {
                        for (String msg : messages.getInfo()) {
                            msg = setPlaceholders(msg, p);
                            p.sendMessage(msg);
                        }
                    }
                }
            }
        } else {
            String arg = args[0];
            switch (arg) {
                case "start": {
                    if (!sender.hasPermission("eventdelay.admin")) {
                        sender.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    eventDelayAPI.setActivationStatus("false");
                    triggers.startRandomEvent();
                    eventDelayAPI.setDelay(config.getTimer());
                    break;
                }
                case "stop": {
                    if (!sender.hasPermission("eventdelay.admin")) {
                        sender.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    if (assistants.isEventActive()) {
                        eventDelayAPI.setOpeningTimer(0);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + assistants.getNowEventPrefix() + " остановлен."));
                        triggers.stopEvent(eventDelayAPI.getNowEvent());
                    } else {
                        sender.sendMessage("Нету активных ивентов");
                    }
                    break;
                }
                case "timer": {

                    if (args.length < 3) {

                        sender.sendMessage(hex("[HELP] Сбросить таймер /event timer reset <duration/activation/delay>"));
                        sender.sendMessage(hex("[HELP] Поставить значение /event timer set <duration/activation/delay> <в секундах>"));

                        return true;
                    }
                    if (args[1].equalsIgnoreCase("reset")) {
                        if (args[2].equalsIgnoreCase("duration")) {

                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(config.getEvents().get(eventDelayAPI.getNowEvent()).getDuration());
                                sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                            } else {
                                sender.sendMessage(hex("Нету активного ивента."));
                            }


                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(config.getEvents().get(eventDelayAPI.getNowEvent()).getActivationTime());
                                sender.sendMessage("Вы успешно сбросили время для таймера "+args[2]);
                            } else {
                                sender.sendMessage(hex("Нету активного ивента."));
                            }

                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(config.getTimer());
                            sender.sendMessage("Вы успешно сбросили время для таймера "+args[2]);
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }
                        sender.sendMessage("Вы успешно сбросили время до начала ивента");
                        break;
                    }
                    if (args[1].equalsIgnoreCase("set") && args.length == 4) {
                        if (args[2].equalsIgnoreCase("duration")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("[HELP] У таймера " + args[2] + " успешно установлено значение " + args[3] + "."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }
                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("[HELP] У таймера "+args[2]+" успешно установлено значение "+args[3]+"."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }
                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(Integer.parseInt(args[3]));
                            sender.sendMessage(hex("[HELP] У таймера "+args[2]+" успешно установлено значение "+args[3]+"."));
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }
                    }

                    break;
                }
                case "setNext": {
                    if (!sender.hasPermission("eventdelay.admin")) {
                        sender.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("Используйте /event next <название ивента>");
                        return true;
                    }

                    if (config.getEvents().containsKey(args[1])) {
                        eventDelayAPI.setNextEvent(args[1]);
                        sender.sendMessage("Следующий ивент установлен на: " + ChatColor.GREEN + eventDelayAPI.getNextEvent());
                    } else {
                        sender.sendMessage("Ивент с названием " + ChatColor.RED + args[1] + ChatColor.WHITE + " не найден.");
                    }
                    break;
                }
                case "reload": {
                    if (!sender.hasPermission("eventdelay.admin")) {
                        sender.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    sender.sendMessage(messages.getReload());
                    plugin.setCfg(new Config(plugin));
                    config.load();
                    final FileConfiguration messagesFile = config.getFileConfiguration(plugin.getDataFolder().getAbsolutePath(), "messages.yml");
                    final FileConfiguration webhookFile = config.getFileConfiguration(plugin.getDataFolder().getAbsolutePath(), "webhook.yml");
                    plugin.getMessages().load(messagesFile);
                    plugin.getWebhookConfig().load(webhookFile);

                    eventDelayAPI.setOpeningTimer(config.getTimer());

                    eventDelayAPI.setTimer(config.getTimer());
                    eventDelayAPI.setFreeze(config.isFreeze());
                    eventDelayAPI.setMinPlayers(config.getMinPlayers());

                    eventDelayAPI.setPreviousEvent("none");
                    eventDelayAPI.setNowEvent("none");
                    eventDelayAPI.setNextEvent("none");

                    timer.initialize();
                    timer.startTimer();
                    assistants.createNextRandomEvent();

                    break;
                }
                case "activate": {
                    if (!sender.hasPermission("eventdelay.admin")) {
                        sender.sendMessage(messages.getNoPerm());
                        return true;
                    }
                    if (config.getEvents().containsKey(eventDelayAPI.getNowEvent())) {
                        if (assistants.isEventActive()) {
                            timer.Activate();
                            Logger.success("Ивент " + assistants.getNowEventPrefix() + " активирован.");
                         } else {
                            Logger.warn("Нету активного ивента чтобы активировать.");
                        }
                    }
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return List.of();
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (p.hasPermission("eventdelay.admin")) {
                completions.add("delay");
                completions.add("info");
                if (assistants.isCompass()) {
                    completions.add("compass");
                }
                completions.add("activate");
                completions.add("setNext");
                completions.add("timer");
                completions.add("start");
                completions.add("stop");
                completions.add("reload");
            } else {
                if (assistants.isCompass()) {
                    completions.add("compass");
                }
                completions.add("delay");
                completions.add("info");
            }

            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args[0].equalsIgnoreCase("timer")) {
            if (args.length==2) {
                completions.add("set");
                completions.add("reset");
                String input = args[1].toLowerCase();
                return completions.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
            if (args.length==3) {
                completions.add("duration");
                completions.add("activation");
                completions.add("delay");

                String input = args[2].toLowerCase();
                return completions.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setNext")
                    || args[0].equalsIgnoreCase("start")) {

                List<String> events = new ArrayList<>(config.getEvents().keySet());
                if (!events.isEmpty()) {
                    completions.addAll(events);
                }

                String input = args[1].toLowerCase();
                return completions.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}