package com.lokamc.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.join;
import static org.bukkit.ChatColor.stripColor;

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
}
