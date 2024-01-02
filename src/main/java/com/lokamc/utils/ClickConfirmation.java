package com.lokamc.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Strings;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.bukkit.ChatColor.*;

public class ClickConfirmation {
    private static final ClickConfirmation instance = new ClickConfirmation();
    private final Map<UUID, CommandMap> commands = new HashMap<>();

    private static class CommandMap {
        private final Cache<UUID, Map<UUID, Consumer<Player>>> commands;
        private final Map<UUID, Map<UUID, Consumer<Player>>> persistentCommands = new HashMap<>();

        CommandMap() {
            commands = Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.MINUTES)
                    .build();
        }

        void addCommand(UUID id, Map<UUID, Consumer<Player>> command, boolean expires) {
            if (expires) {
                commands.put(id, command);
            } else {
                persistentCommands.put(id, command);
            }
        }

        void runCommand(Player p, UUID setId, UUID commandId) {
            Map<UUID, Consumer<Player>> map = commands.getIfPresent(setId);
            if (map != null) {
                Consumer<Player> command = map.get(commandId);
                if (command != null) {
                    command.accept(p);
                    commands.invalidate(setId);
                }
            } else {
                map = persistentCommands.get(setId);
                if (map != null) {
                    Consumer<Player> command = map.get(commandId);
                    if (command != null) {
                        command.accept(p);
                    }
                }
            }
        }
    }

    private ClickConfirmation() {
    }

    public void getConfirmation(Player p, FancyMessage question, String yesResponse, Consumer<Player> yesCommand) {
        if (question != null) question.send(p);
        sendQuestion(p, yesResponse, yesCommand, "Cancel", null);
    }

    public void getConfirmation(Player p, String question, String yesResponse, Consumer<Player> yesCommand) {
        getConfirmation(p, question, yesResponse, yesCommand, "Cancel", null);
    }

    public void getConfirmation(Player p, String question, String yesResponse, Consumer<Player> yesCommand, String noResponse, Consumer<Player> noCommand) {
        if (!Strings.isNullOrEmpty(question)) p.sendMessage(question);
        sendQuestion(p, yesResponse, yesCommand, noResponse, noCommand);
    }

    private void sendQuestion(Player p, String yesResponse, Consumer<Player> yesCommand, String noResponse, Consumer<Player> noCommand) {
        UUID setId = UUID.randomUUID();
        UUID yesId = UUID.randomUUID();

        Map<UUID, Consumer<Player>> setMap = new HashMap<>();
        setMap.put(yesId, yesCommand);

        FancyMessage msg = new FancyMessage("" + GREEN + BOLD + "[" + yesResponse + "]")
                .style(BOLD)
                .command("/confirm " + setId + " " + yesId)
                .tooltip("Click to confirm!");

        if (noResponse != null) {
            UUID noId = UUID.randomUUID();
            setMap.put(noId, noCommand);
            msg.then(" ")
                    .then("" + RED + BOLD + "[" + noResponse + "]")
                    .style(BOLD)
                    .command("/confirm " + setId + " " + noId)
                    .tooltip("Click to deny.");
        }

        registerCommands(p.getUniqueId(), setId, setMap, true);
        msg.send(p);
    }

    public String registerClick(Player p, Consumer<Player> consumer) {
        return registerClick(p, consumer, true);
    }

    public String registerClick(Player p, Consumer<Player> consumer, boolean expires) {
        UUID id = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        registerCommands(p.getUniqueId(), setId, Map.of(id, consumer), expires);
        return "/confirm " + setId + " " + id;
    }

    public void registerCommands(UUID id, UUID setId, Map<UUID, Consumer<Player>> commandMap, boolean expires) {
        CommandMap map = commands.computeIfAbsent(id, uuid -> new CommandMap());
        map.addCommand(setId, commandMap, expires);
        commands.put(id, map);
    }

    public void removeCommands(Player p) {
        commands.remove(p.getUniqueId());
    }

    public static ClickConfirmation getInstance() {
        return instance;
    }

    public void respondConsumer(Player p, String[] args) {
        CommandMap map = commands.get(p.getUniqueId());
        if (map != null) {
            UUID setId = UUID.fromString(args[0]);
            UUID commandId = UUID.fromString(args[1]);
            map.runCommand(p, setId, commandId);
        }
    }
}
