package com.lokamc.utils;

import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class FancyMessage {
    private Component component;
    private Component lastThenSet;
    private UUID setId;
    private Map<UUID, Consumer<Player>> commandsMap = new HashMap<>();
    private boolean expires = true;

    public FancyMessage() {
        this("");
    }

    public FancyMessage(String text) {
        component = Component.empty();
        then(text);
    }

    public FancyMessage(Component component) {
        this.component = component;
    }

    public FancyMessage clone() {
        FancyMessage clone = new FancyMessage(component);
        clone.setId = setId;
        clone.commandsMap = new HashMap<>(commandsMap);
        clone.expires = expires;
        return clone;
    }

    public Component getComponent() {
        return component;
    }

    public FancyMessage setExpires(boolean expires) {
        this.expires = expires;
        return this;
    }

    public FancyMessage then(Component component) {
        this.component = this.component.append(component);
        return this;
    }

    public FancyMessage then(String text) {
        return then(text, null, null);
    }

    public FancyMessage then(String text, TextColor color) {
        return then(text, color, null);
    }

    public FancyMessage then(String text, TextColor color, TextDecoration style) {
        if (text == null) return this;

        if (text.contains("http")) {
            if (text.contains("http")) {
                int index = text.indexOf("http");
                while (true) {
                    int nextIndex = text.indexOf(" ", index + 1);
                    then(text.substring(0, index));

                    String link = text.substring(index, nextIndex == -1 ? text.length() : nextIndex);
                    TextComponent linkPart = Component.text(link, component.color(), ITALIC)
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, link));
                    component = component.append(linkPart);

                    text = text.substring(nextIndex == -1 ? text.length() : nextIndex);
                    index = text.indexOf("http");
                    if (index == -1) {
                        if (!text.isEmpty()) {
                            then(text);
                        }
                        return this;
                    }
                }
            }
        }

        Component component;
        if (color != null) {
            if (style != null) {
                component = Component.text(text, color, style);
            } else {
                component = Component.text(text, color);
            }
        } else if (style != null) {
            component = Component.text(text).style(Style.style(style));
        } else {
            component = Component.text(text);
        }

        this.component = this.component.append(component);
        return this;
    }

    public FancyMessage color(TextColor color) {
        component = component.color(color);
        return this;
    }

    public FancyMessage color(String color) {
        TextColor newColor = TextColor.fromHexString(color);
        component = component.color(newColor);
        return this;
    }

    public FancyMessage style(TextDecoration style) {
        component = component.style(Style.style(style));
        return this;
    }

    public FancyMessage itemTooltip(ItemStack itemStack) {
        component = component.hoverEvent(itemStack);
        return this;
    }

    public FancyMessage tooltip(List<String> tooltip) {
        ComponentLike[] components = tooltip.stream().map(PaperComponents.plainSerializer()::deserialize).toArray(ComponentLike[]::new);
        component = component.hoverEvent(TextComponent.ofChildren(components));
        return this;
    }

    public FancyMessage tooltip(String... tooltip) {
        ComponentLike[] components = Arrays.stream(tooltip).map(PaperComponents.plainSerializer()::deserialize).toArray(ComponentLike[]::new);
        component = component.hoverEvent(TextComponent.ofChildren(components));
        return this;
    }

    public FancyMessage link(String url) {
        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    public FancyMessage suggest(String text) {
        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
        return this;
    }

    public FancyMessage command(String command) {
        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public FancyMessage command(Consumer<Player> command) {
        if (setId == null) {
            setId = UUID.randomUUID();
        }

        UUID commandId = UUID.randomUUID();
        commandsMap.put(commandId, command);
        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/confirm " + setId + " " + commandId));
        return this;
    }

    public String toOldMessageFormat() {
        return component.toString();
    }

    public String toJson() {
        return ComponentSerializer.toString(build(null));
    }

    public static FancyMessage fromJson(String json) {
        return new FancyMessage();
    }

    public void send(Player p) {
        if (p == null) return;

        p.sendMessage(build(p.getUniqueId()));
    }

    public Component build(UUID playerId) {
        if (!commandsMap.isEmpty() && playerId != null) {
            ClickConfirmation.getInstance().registerCommands(playerId, setId, commandsMap, expires);
        }

//        TextComponent finalComponent = new TextComponent();
//        for (Component component : componentList) {
//            finalComponent.addExtra(component);
//        }
        return component;
    }
}