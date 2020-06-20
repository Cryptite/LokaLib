package utils;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class FancyMessage {
    private final List<TextComponent> componentList;
    private List<TextComponent> lastThenSet;
    private UUID setId;
    private final Map<UUID, Consumer<Player>> commandsMap = new HashMap<>();
    private net.md_5.bungee.api.ChatColor currentColor = net.md_5.bungee.api.ChatColor.WHITE;
    private boolean expires = true;

    public FancyMessage() {
        this("");
    }

    public FancyMessage(String text) {
        componentList = new ArrayList<>();
        then(text);
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

                    ChatColor color = ChatColor.getByChar(text.substring(index + 1, index + 2));
                    String nextPart = text.substring(index + 2, nextIndex == -1 ? text.length() : nextIndex);
                    if (!nextPart.equalsIgnoreCase(""))
                        next.setText(nextPart);

                    net.md_5.bungee.api.ChatColor colorChar = net.md_5.bungee.api.ChatColor.getByChar(color.getChar());
                    switch (colorChar) {
                        case BOLD:
                            next.setBold(true);
                            break;
                        case STRIKETHROUGH:
                            next.setStrikethrough(true);
                            break;
                        case UNDERLINE:
                            next.setUnderlined(true);
                            break;
                        case ITALIC:
                            next.setItalic(true);
                            break;
                        case RESET:
                            break;
                        default:
                            next.setColor(colorChar);
                            break;
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

    public FancyMessage color(ChatColor color) {
        net.md_5.bungee.api.ChatColor bColor = net.md_5.bungee.api.ChatColor.getByChar(color.getChar());
        latest().setColor(bColor);
        currentColor = bColor;
        return this;
    }

    public FancyMessage style(ChatColor style) {
        switch (style) {
            case BOLD:
                latest().setBold(true);
                break;
            case STRIKETHROUGH:
                latest().setStrikethrough(true);
                break;
            case UNDERLINE:
                latest().setUnderlined(true);
                break;
            case ITALIC:
                latest().setItalic(true);
                break;
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
