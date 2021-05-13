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
    private final Component component;
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

    public FancyMessage(Component[] baseComponents) {
        component = Component.empty();
        for (Component baseComponent : baseComponents) {
            component.append(baseComponent);
        }
    }

    public FancyMessage clone() {
        FancyMessage clone = new FancyMessage();
//        clone.componentList = componentList.stream().map(TextComponent::duplicate).collect(Collectors.toList());
//        clone.lastThenSet = lastThenSet.stream().map(TextComponent::duplicate).collect(Collectors.toList());
        clone.setId = setId;
        clone.commandsMap = new HashMap<>(commandsMap);
//        clone.currentColor = currentColor;
        clone.expires = expires;
        return clone;
    }

    public FancyMessage setExpires(boolean expires) {
        this.expires = expires;
        return this;
    }

    public FancyMessage then(String text) {
        if (text.contains("http")) {
            if (text.contains("http")) {
                int index = text.indexOf("http");
                while (true) {
                    int nextIndex = text.indexOf(" ", index + 1);
                    then(text.substring(0, index));

                    String link = text.substring(index, nextIndex == -1 ? text.length() : nextIndex);
                    TextComponent linkPart = Component.text(link, component.color(), ITALIC)
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, link));
                    component.append(linkPart);

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

        component.append(Component.text(text));
        return this;
    }

    public FancyMessage color(TextColor color) {
        component.color(color);
        return this;
    }

    public FancyMessage color(String color) {
        TextColor newColor = TextColor.fromHexString(color);
        component.color(newColor);
        return this;
    }

    public FancyMessage style(TextDecoration style) {
        component.style(Style.style(style));
        return this;
    }

    public FancyMessage itemTooltip(ItemStack itemStack) {
        component.hoverEvent(itemStack);
        return this;
    }

    public FancyMessage tooltip(List<String> tooltip) {
        ComponentLike[] components = tooltip.stream().map(PaperComponents.plainSerializer()::deserialize).toArray(ComponentLike[]::new);
        component.hoverEvent(TextComponent.ofChildren(components));
        return this;
    }

    public FancyMessage tooltip(String... tooltip) {
        ComponentLike[] components = Arrays.stream(tooltip).map(PaperComponents.plainSerializer()::deserialize).toArray(ComponentLike[]::new);
        component.hoverEvent(TextComponent.ofChildren(components));
        return this;
    }

    public FancyMessage link(String url) {
        component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    public FancyMessage suggest(String text) {
        component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
        return this;
    }

    public FancyMessage command(String command) {
        component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public FancyMessage command(Consumer<Player> command) {
        if (setId == null) {
            setId = UUID.randomUUID();
        }

        UUID commandId = UUID.randomUUID();
        commandsMap.put(commandId, command);
        component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/confirm " + setId + " " + commandId));
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
