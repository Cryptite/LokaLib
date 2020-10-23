package com.lokamc.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;

public class FancyMessage {
    private List<TextComponent> componentList;
    private List<TextComponent> lastThenSet;
    private UUID setId;
    private Map<UUID, Consumer<Player>> commandsMap = new HashMap<>();
    private net.md_5.bungee.api.ChatColor currentColor = net.md_5.bungee.api.ChatColor.WHITE;
    private boolean expires = true;

    public FancyMessage() {
        this("");
    }

    public FancyMessage(String text) {
        componentList = new ArrayList<>();
        then(text);
    }

    public FancyMessage(BaseComponent[] baseComponents) {
        componentList = new ArrayList<>();
        for (BaseComponent baseComponent : baseComponents) {
            if (baseComponent instanceof TextComponent) {
                TextComponent txt = (TextComponent) baseComponent;
                componentList.addAll(txt.getExtra().stream().map(baseComponent1 -> (TextComponent) baseComponent1).collect(Collectors.toList()));
            }
        }
    }

    public FancyMessage clone() {
        FancyMessage clone = new FancyMessage();
        clone.componentList = componentList.stream().map(TextComponent::duplicate).collect(Collectors.toList());
        clone.lastThenSet = lastThenSet.stream().map(TextComponent::duplicate).collect(Collectors.toList());
        clone.setId = setId;
        clone.commandsMap = new HashMap<>(commandsMap);
        clone.currentColor = currentColor;
        clone.expires = expires;
        return clone;
    }

    public FancyMessage setExpires(boolean expires) {
        this.expires = expires;
        return this;
    }

    public FancyMessage then(String text) {
        lastThenSet = new ArrayList<>();
        if (text.contains("§") || text.contains("http")) {
            if (text.contains("http")) {
                int index = text.indexOf("http");
                while (true) {
                    int nextIndex = text.indexOf(" ", index + 1);
                    then(text.substring(0, index));

                    String link = text.substring(index, nextIndex == -1 ? text.length() : nextIndex);
                    TextComponent linkPart = new TextComponent(link);
                    linkPart.setColor(currentColor);
                    linkPart.setItalic(true);
                    linkPart.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                    componentList.add(linkPart);
                    lastThenSet.add(linkPart);

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

            if (text.contains("§")) {
                int index = text.indexOf("§");

                //Index of first color may be much later, so we need to add everything up until that
                //next color with whatever the last set color was.
                TextComponent beforeFirstColor = new TextComponent(text.substring(0, index));
                if (currentColor != null)
                    beforeFirstColor.setColor(currentColor);
                lastThenSet.add(beforeFirstColor);

                //Then we can move on to doing more color parsing
                TextComponent next = new TextComponent("");

                do {
                    int nextIndex = text.indexOf("§", index + 1);

                    ChatColor color = ChatColor.getByChar(text.charAt(index + 1));
                    String nextPart = text.substring(index + 2, nextIndex == -1 ? text.length() : nextIndex);
                    if (!nextPart.equalsIgnoreCase(""))
                        next.setText(nextPart);

                    if (BOLD.equals(color)) {
                        next.setBold(true);
                    } else if (STRIKETHROUGH.equals(color)) {
                        next.setStrikethrough(true);
                    } else if (UNDERLINE.equals(color)) {
                        next.setUnderlined(true);
                    } else if (ITALIC.equals(color)) {
                        next.setItalic(true);
                    } else if (RESET.equals(color)) {
                    } else {
                        next.setColor(color);
                    }
                    if (!nextPart.equalsIgnoreCase("")) {
                        lastThenSet.add(next);
                        next = new TextComponent("");
                    }
                    text = text.substring(index + 2);
                    index = text.indexOf("§");
                } while (index != -1);
            }

            componentList.addAll(lastThenSet);
        } else {
            TextComponent textComponent = new TextComponent(text);
            if (currentColor != null) {
                textComponent.setColor(currentColor);
            }
            componentList.add(textComponent);
            lastThenSet.add(textComponent);
        }
        return this;
    }

    public FancyMessage color(org.bukkit.ChatColor color) {
        net.md_5.bungee.api.ChatColor bColor = ChatColor.getByChar(color.getChar());
        latest().setColor(bColor);
        currentColor = bColor;
        return this;
    }

    public FancyMessage color(ChatColor color) {
        latest().setColor(color);
        currentColor = color;
        return this;
    }

    public FancyMessage color(String color) {
        ChatColor newColor = of(color);
        latest().setColor(newColor);
        currentColor = newColor;
        return this;
    }

    public FancyMessage style(org.bukkit.ChatColor style) {
        return style(ChatColor.getByChar(style.getChar()));
    }

    public FancyMessage style(ChatColor style) {
        if (BOLD.equals(style)) {
            latest().setBold(true);
        } else if (STRIKETHROUGH.equals(style)) {
            latest().setStrikethrough(true);
        } else if (UNDERLINE.equals(style)) {
            latest().setUnderlined(true);
        } else if (ITALIC.equals(style)) {
            latest().setItalic(true);
        }
        return this;
    }

    public FancyMessage itemTooltip(ItemStack itemStack) {
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                new BaseComponent[]{new TextComponent(convertItemStackToJson(itemStack))});
        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setHoverEvent(hoverEvent);
        }
//        latest().setHoverEvent(hoverEvent);
        return this;
    }

    /**
     * Converts an {@link ItemStack} to a Json string
     * for sending with {@link BaseComponent}'s.
     *
     * @param itemStack the item to convert
     * @return the Json string representation of the item
     */
    private String convertItemStackToJson(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
            return null;
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }

    public FancyMessage tooltip(List<String> tooltip) {
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(String.join("\n", tooltip)).create());
        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setHoverEvent(hoverEvent);
        }
        return this;
    }

    public FancyMessage tooltip(String... tooltip) {
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(String.join("\n", tooltip)).create());
        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setHoverEvent(hoverEvent);
        }
        return this;
    }

    public FancyMessage link(String url) {
        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        }
        return this;
    }

    public FancyMessage suggest(String text) {
        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
        }
        return this;
    }

    public FancyMessage command(String command) {
        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
        return this;
    }

    public FancyMessage command(Consumer<Player> command) {
        if (setId == null) {
            setId = UUID.randomUUID();
        }

        UUID commandId = UUID.randomUUID();
        commandsMap.put(commandId, command);

        for (TextComponent component : componentList) {
            if (lastThenSet.contains(component))
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/confirm " + setId + " " + commandId));
        }
        return this;
    }

    private TextComponent latest() {
        if (componentList.isEmpty()) {
            return new TextComponent("");
        }
        return componentList.get(componentList.size() - 1);
    }

    public List<TextComponent> getComponents() {
        return componentList;
    }

    public String toOldMessageFormat() {
        return build(null).toLegacyText();
    }

    public String toJson() {
        return ComponentSerializer.toString(build(null));
    }

    public void send(Player p) {
        if (p == null) return;

        p.sendMessage(build(p.getUniqueId()));
    }

    public TextComponent build(UUID playerId) {
        if (!commandsMap.isEmpty() && playerId != null) {
            ClickConfirmation.getInstance().registerCommands(playerId, setId, commandsMap, expires);
        }

        TextComponent finalComponent = new TextComponent();
        for (TextComponent component : componentList) {
            finalComponent.addExtra(component);
        }
        return finalComponent;
    }
}
