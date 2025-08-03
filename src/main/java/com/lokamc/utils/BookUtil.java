package com.lokamc.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.map.MinecraftFont;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.bukkit.ChatColor.stripColor;
import static org.bukkit.Material.WRITTEN_BOOK;

/**
 * Create a "Virtual" book gui that doesn't require the user to have a book in their hand.
 * Requires ReflectionUtil class.
 * Built for Minecraft 1.9
 *
 * @author Jed
 */
public class BookUtil {
    private static final MinecraftFont font = new MinecraftFont();
    public static final int maxLineWidth = font.getWidth("LLLLLLLLLLLLLLLLLL");

    /**
     * Splits a string given a line length.
     * If the string contains \n, returns the string just based on a split of the newline characters
     *
     * @param text
     * @return
     */
    public static List<String> splitIntoPages(String text) {
        return splitIntoPages(text, null);
    }

    /**
     * Splits a string given a line length.
     * If the string contains \n, returns the string just based on a split of the newline characters
     *
     * @param text
     * @return
     */
    public static List<String> splitIntoPages(String text, String eachPageHeader) {
        if (text == null) return new ArrayList<>();

        List<String> lines = new ArrayList<>();

        int lineLength = 0;
        List<String> currentLineWords = new ArrayList<>();
        String lastColorCode = null;
        for (String word : text.split(" ")) {
            if (word.contains("§")) {
                lastColorCode = "";
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
                        lastColorCode = "";
                    } else {
                        lastColorCode += colorCode;
                    }
                    startIndex += 2;
                }
            }

            lineLength += getFontLength(word);

            for (String split : word.split("\n")) {
                if (split.isEmpty()) {
                    if (!currentLineWords.isEmpty()) {
                        lines.add(join(currentLineWords, " "));
                    }

                    lines.add("");
                    currentLineWords.clear();
                    currentLineWords.add((lastColorCode != null ? lastColorCode : ""));
                    lineLength = 0;
                } else {
                    if (lineLength > maxLineWidth) {
                        lines.add(join(currentLineWords, " "));
                        currentLineWords.clear();
                        currentLineWords.add((lastColorCode != null ? lastColorCode : "") + split);
                        lineLength = 0;
                    } else {
                        currentLineWords.add(split);
                    }
                }
            }

        }

        lines.add(join(currentLineWords, " "));

        List<String> pages = new ArrayList<>();
        int currentLine = 1;
        StringBuilder pageBuilder = new StringBuilder();
        while (!lines.isEmpty()) {
            String line;
            if (currentLine == 1 && eachPageHeader != null) {
                pageBuilder.append(eachPageHeader).append("\n");
                currentLine++;
            } else {
                line = lines.remove(0);

                if (line.isEmpty()) {
                    pageBuilder.append("\n");
                } else {
                    pageBuilder.append(line).append(" ");
                }

                if (currentLine++ >= 9) {
                    pages.add(processPage(pageBuilder.toString()));
                    pageBuilder = new StringBuilder();
                    currentLine = 1;
                }
            }
        }

        pages.add(processPage(pageBuilder.toString()));

        return pages;
    }

    private static String processPage(String page) {
        while (page.startsWith("\n")) {
            page = page.substring(2);
        }
        return page;
    }

    private static int getFontLength(String word) {
        //Current line + word is too long to be one line
        String replacedString = stripColor(word);
        for (char ch : word.toCharArray()) {
            if (font.getChar(ch) == null) {
                replacedString = replacedString.replace(ch, '-');
            }
        }

        return font.getWidth(replacedString);
    }

    public static List<String> getLines(String rawText) {
        //Note that the only flaw with using MinecraftFont is that it can't account for some UTF-8 symbols, it will throw an IllegalArgumentException
        final MinecraftFont font = new MinecraftFont();
        final int maxLineWidth = font.getWidth("LLLLLLLLLLLLLLLLLLL");

        //Get all of our lines
        List<String> lines = new ArrayList<>();
        try {
            //Each 'section' is separated by a line break (\n)
            for (String section : rawText.split("\\\\n")) {
                //If the section is blank, that means we had a double line break there
                if (section.isEmpty())
                    lines.add("\n\n");
                    //We have an actual section with some content
                else {
                    //Iterate through all the words of the section
                    String[] words = ChatColor.stripColor(section).split(" ");
                    String line = "";
                    for (int index = 0; index < words.length; index++) {
                        String word = words[index];
                        //Make sure we can actually use this next word in our current line
                        String test = (line + " " + word);
                        if (test.startsWith(" ")) test = test.substring(1);
                        //Current line + word is too long to be one line
                        if (font.getWidth(test) > maxLineWidth) {
                            //Add our current line
                            lines.add(line);
                            //Set our next line to start off with this word
                            line = word;
                            continue;
                        }
                        //Add the current word to our current line
                        line = test;
                    }
                    //Make sure we add the line if it was the last word and wasn't too long for the line to start a new one
                    if (!line.isEmpty())
                        lines.add(line);
                }
            }
        } catch (IllegalArgumentException ex) {
            lines.clear();
        }

        return lines;
    }

    public static ItemStack getShowableBook(ItemStack book) {
        if (book.getType() == WRITTEN_BOOK) return book;

        ItemStack writtenBook = new ItemStack(WRITTEN_BOOK);
        if (book.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.title(Component.text("Unknown"));
            bookMeta.author(Component.text("Unknown"));
            writtenBook.setItemMeta(bookMeta);
        }

        return writtenBook;
    }
}
