package com.lokamc.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.GRAY;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.join;
import static org.bukkit.ChatColor.stripColor;

public class StringUtils {
    private static final DecimalFormat numberFormat = new DecimalFormat("#,###,###");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    public static String stripComponentColor(Component component) {
        return ChatColor.stripColor(componentToPlain(component));
    }

    /**
     * Converts a legacy section-formatted ({@code §}) string into a modern Adventure {@link Component}.
     */
    public static Component legacyToComponent(String legacy) {
        if (legacy == null) return Component.empty();
        return LEGACY.deserialize(legacy);
    }

    /**
     * Serializes a {@link Component} back to a legacy section-formatted ({@code §}) string.
     */
    public static String componentToLegacy(Component component) {
        if (component == null) return "";
        return LEGACY.serialize(stripObjects(component));
    }

    /**
     * Flattens a {@link Component} to plain text with all formatting stripped.
     */
    public static String componentToPlain(Component component) {
        if (component == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(stripObjects(component));
    }

    /**
     * Removes {@link ObjectComponent}s (player heads, atlas sprites) from the tree.
     * <p>
     * They have no textual form, and Adventure's flattener renders them as the literal text
     * {@code [unknown player head]}, which would otherwise end up in Discord relays and console logs anywhere a
     * chat format is serialized. The space that separates a head from the name following it is dropped along with
     * it, so the text reads the same as it did before heads were added to the chat formats.
     */
    public static Component stripObjects(Component component) {
        if (component == null) return Component.empty();

        List<Component> children = component.children();
        if (children.isEmpty()) {
            return component instanceof ObjectComponent ? Component.empty() : component;
        }

        List<Component> kept = new ArrayList<>(children.size());
        boolean afterObject = false;
        for (Component child : children) {
            if (child instanceof ObjectComponent) {
                afterObject = true;
                child.children().forEach(nested -> kept.add(stripObjects(nested)));
                continue;
            }

            if (afterObject && child instanceof TextComponent text && text.content().startsWith(" ")) {
                child = text.content(text.content().substring(1));
            }

            afterObject = false;
            kept.add(stripObjects(child));
        }

        if (component instanceof ObjectComponent) {
            return Component.textOfChildren(kept.toArray(new Component[0]));
        }

        return component.children(kept);
    }

    /**
     * Adds {@link ClickEvent#openUrl} click events (and italics) to any {@code http(s)://} URLs found
     * in the component, mirroring the auto-linking the old FancyMessage did in {@code then(String)}.
     */
    public static Component linkify(Component component) {
        if (component == null) return Component.empty();
        return component.replaceText(builder -> builder
                .match(URL_PATTERN)
                .replacement((match, b) -> b.clickEvent(ClickEvent.openUrl(match.group()))
                        .decorate(TextDecoration.ITALIC)));
    }

    public static String getFormattedNumber(double number) {
        return numberFormat.format(number);
    }

    /**
     * Splits a string once it's reached 30 characters or more while retaining color codes
     * If the string contains \n, returns the string just based on a split of the newline characters
     *
     * @param text
     * @return
     */
    public static List<String> wordWrap(String text) {
        return wordWrap(text, 30);
    }

    /**
     * Splits a string given a line length while retaining color codes
     * If the string contains \n, returns the string just based on a split of the newline characters
     *
     * @param text
     * @param maxLineLength
     * @return
     */
    public static List<String> wordWrap(String text, int maxLineLength) {
        if (text == null) return new ArrayList<>();

        if (text.contains("\\n")) {
            List<String> firstLines = new ArrayList<>();
            for (String line : text.replace("\\n", "\n").split("\n")) {
                firstLines.addAll(wordWrap(line, maxLineLength));
            }

            return firstLines;
        }

        List<String> lines = new ArrayList<>();

        int lineLength = 0;
        List<String> currentLineWords = new ArrayList<>();
        StringBuilder lastColorCode = null;
        for (String word : text.split(" ")) {
            if (word.contains("§")) {
                lastColorCode = new StringBuilder();
                int startIndex = 0;
                while (word.substring(startIndex).contains("§")) {
                    for (int i = startIndex; i < word.length(); i++) {
                        if (word.charAt(i) == '§') {
                            startIndex = i;
                            break;
                        }
                    }

                    String colorCode = word.substring(startIndex, startIndex + 2);
                    if (colorCode.charAt(1) == 'r') {
                        lastColorCode = new StringBuilder();
                    } else {
                        lastColorCode.append(colorCode);
                    }
                    startIndex += 2;
                }
            }

            if (lineLength > maxLineLength) {
                lines.add(join(currentLineWords, " "));
                currentLineWords.clear();
                currentLineWords.add((lastColorCode != null ? lastColorCode.toString() : "") + word);
                lineLength = 0;
            } else {
                currentLineWords.add(word);
            }

            lineLength += stripColor(word).length();
        }
        lines.add(join(currentLineWords, " "));
        return lines;
    }

    public static String getChatColor(String text, String specificWord) {
        StringBuilder lastColorCode = null;
        for (String word : text.split(" ")) {
            if (word.contains("§")) {
                lastColorCode = new StringBuilder();
                int startIndex = 0;
                while (word.substring(startIndex).contains("§")) {
                    for (int i = startIndex; i < word.length(); i++) {
                        if (word.charAt(i) == '§') {
                            startIndex = i;
                            break;
                        }
                    }

                    String colorCode = word.substring(startIndex, startIndex + 2);
                    if (colorCode.charAt(1) == 'r') {
                        lastColorCode = new StringBuilder();
                    } else {
                        lastColorCode.append(colorCode);
                    }
                    startIndex += 2;

                }
            }

            if (stripColor(word).equals(specificWord)) {
                return lastColorCode != null ? lastColorCode.toString() : "";
            }
        }

        return "";
    }

    public static Date getFutureDate(Player p, String text) {
        String time;

        switch (text) {
            case "-1":
            case "0":
                break;
            default:
                time = text.substring(text.length() - 1);
                if (!time.equals("d") && !time.equals("m") && !time.equals("h") && !time.equals("s") && !time.equals("w")) {
                    p.sendMessage(GRAY + "Must end in s (seconds), m (minutes), h (hours), or d (days), or w (weeks). Eg: 24h or 0 for "
                            + "non-repeatable.");
                    return null;
                } else {
                    String number = text.split(time)[0];
                    if (!isNumeric(number)) {
                        p.sendMessage(GRAY + "Provide a number of seconds, minutes, hours, or " +
                                "days (eg: 24h or 0 for non-repeatable)");
                        return null;
                    }
                }
                break;
        }

        Calendar calendar = Calendar.getInstance();
        int amount = Integer.parseInt(text.substring(0, text.length() - 1));
        if (text.endsWith("h")) {
            calendar.add(Calendar.HOUR, amount);
        } else if (text.endsWith("d")) {
            calendar.add(Calendar.HOUR, amount * 24);
        } else if (text.endsWith("m")) {
            calendar.add(Calendar.MINUTE, amount);
        } else if (text.endsWith("w")) {
            calendar.add(Calendar.HOUR, amount * 24 * 7);
        } else {
            calendar.add(Calendar.SECOND, amount);
        }
        return calendar.getTime();
    }
}
