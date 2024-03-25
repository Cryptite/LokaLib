package com.lokamc.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Strings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.bukkit.ChatColor.*;

public class ClickConfirmation {
    private static final ClickConfirmation instance = new ClickConfirmation();
    private static final Cache<UUID, Map<UUID, Consumer<Player>>> commandsCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private static final Cache<UUID, Map<UUID, Consumer<Player>>> persistentCommands = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

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

        registerCommands(setId, setMap, true);
        msg.send(p);
    }

    public void sendClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer) {
        sendClickableComponent(p, component, hover, consumer, true);
    }

    public void sendClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer, boolean expires) {
        p.sendMessage(getClickableComponent(p, component, hover, consumer, expires));
    }

    public Component getClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer) {
        return getClickableComponent(p, component, hover, consumer, true);
    }

    public Component getClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer, boolean expires) {
        return component
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand(registerClick(consumer, expires)));
    }

    public String registerClick(Player p, Consumer<Player> consumer) {
        return registerClick(consumer, true);
    }

    public String registerClick(Consumer<Player> consumer, boolean expires) {
        UUID id = UUID.randomUUID();
        UUID setId = UUID.randomUUID();
        registerCommands(setId, Map.of(id, consumer), expires);
        return "/confirm " + setId + " " + id;
    }

    public void registerCommands(UUID setId, Map<UUID, Consumer<Player>> commands, boolean expires) {
        if (expires) {
            commandsCache.asMap().computeIfAbsent(setId, s -> commands);
        } else {
            persistentCommands.asMap().computeIfAbsent(setId, s -> commands);
        }
    }

    public void removeCommands(Player p) {
        commandsCache.invalidate(p.getUniqueId());
    }

    public static ClickConfirmation getInstance() {
        return instance;
    }

    public void respondConsumer(Player p, String[] args) {
        UUID setId = UUID.fromString(args[0]);
        UUID commandId = UUID.fromString(args[1]);

        Map<UUID, Consumer<Player>> map = commandsCache.getIfPresent(setId);
        if (map != null && runCommand(p, map, commandId)) {
            commandsCache.invalidate(setId);
            return;
        }

        map = persistentCommands.getIfPresent(setId);
        if (map != null && runCommand(p, map, commandId)) {
            persistentCommands.invalidate(setId);
        }
    }

    private boolean runCommand(Player p, Map<UUID, Consumer<Player>> commands, UUID commandId) {
        Consumer<Player> consumer = commands.get(commandId);
        if (consumer != null) {
            consumer.accept(p);
            return true;
        }

        return false;
    }
}
