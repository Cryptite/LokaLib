package com.lokamc.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.GRAY;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.StringUtils.join;

public class StringUtils {
    private static final DecimalFormat numberFormat = new DecimalFormat("#,###,###");

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

    public static @NonNull String stripColor(String text) {
        return PlainComponentSerializer.plain().serialize(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    public static @NonNull String translateAlternateColorCodes(String text) {
        return translateAlternateColorCodes('&', text);
    }

    public static @NonNull String translateAlternateColorCodes(char altChar, String text) {
        return LegacyComponentSerializer.legacy(altChar).deserialize(text).content();
    }
}
