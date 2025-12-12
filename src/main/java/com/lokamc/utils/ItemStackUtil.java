package com.lokamc.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Strings;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.bukkit.DyeColor.*;

public class ItemStackUtil {
    private static final TreeMap<Integer, String> romanMap = new TreeMap<>();

    static {
        romanMap.put(1000, "M");
        romanMap.put(900, "CM");
        romanMap.put(500, "D");
        romanMap.put(400, "CD");
        romanMap.put(100, "C");
        romanMap.put(90, "XC");
        romanMap.put(50, "L");
        romanMap.put(40, "XL");
        romanMap.put(10, "X");
        romanMap.put(9, "IX");
        romanMap.put(5, "V");
        romanMap.put(4, "IV");
        romanMap.put(1, "I");
    }

    public static ItemStack createItemStack(Material material, String displayName) {
        return createItemStack(material, displayName, -1);
    }

    public static ItemStack createItemStack(Material material, String displayName, int customModelData) {
        return createItemStack(new ItemStack(material), displayName, customModelData);
    }

    public static ItemStack createItemStack(ItemStack itemStack, String displayName) {
        return createItemStack(itemStack, displayName, -1);
    }

    public static ItemStack createItemStack(ItemStack itemStack, String displayName, int customModelData) {
        if (itemStack.isEmpty()) return itemStack;

        itemStack = itemStack.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);

        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemStack(Material material, Component displayName) {
        return createItemStack(material, displayName, -1);
    }

    public static ItemStack createItemStack(Material material, Component displayName, int customModelData) {
        return createItemStack(new ItemStack(material), displayName, customModelData);
    }

    public static ItemStack createItemStack(ItemStack itemStack, Component displayName) {
        return createItemStack(itemStack, displayName, -1);
    }

    public static ItemStack createItemStack(ItemStack itemStack, Component displayName, int customModelData) {
        if (itemStack.isEmpty()) return itemStack;

        itemStack = itemStack.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(displayName);

        if (customModelData != -1) {
            itemMeta.setCustomModelData(customModelData);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean hasDisplayName(ItemStack item, String name) {
        return item.getItemMeta() != null && item.getItemMeta().getDisplayName().equals(name);
    }

    public static boolean alreadyImbued(List<String> lore, String search) {
        for (String line : lore) {
            if (line.contains(search)) return true;
        }

        return false;
    }

    public static String getFriendlyName(Material m) {
        return getFriendlyName(new ItemStack(m));
    }

    public static String getFriendlyName(ItemStack item) {
        if (item == null) return "???";

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return new ItemStack(item.getType()).getI18NDisplayName();

        String displayName = meta.getDisplayName();

        if (Strings.isNullOrEmpty(displayName)) {
            if (item.getType() == Material.PLAYER_HEAD || item.getType() == Material.PLAYER_WALL_HEAD) {
                SkullMeta headMeta = (SkullMeta) meta;
                PlayerProfile playerProfile = headMeta.getPlayerProfile();
                if (playerProfile != null) {
                    String headName = playerProfile.getName();
                    if (headName != null) {
                        displayName = headName;
                    } else {
                        displayName = item.getI18NDisplayName();
                    }
                } else {
                    displayName = "Player Head";
                }
            } else {
                displayName = item.getI18NDisplayName();
            }

        }

        if (displayName != null && displayName.equalsIgnoreCase("air")) {
            displayName = capitalize(item.getType().toString().toLowerCase().replace("_", " "));
        }

        return displayName;
    }

    public static String getEnchantFriendlyName(Enchantment enchantment, int level) {
        return getEnchantmentName(enchantment) + " " + toRoman(level);
    }

    public static String getEnchantmentName(Enchantment ench) {
        switch (ench.getName()) {
            case "DAMAGE_ALL":
                return "Sharpness";
            case "DAMAGE_ARTHROPODS":
                return "Bane Of Arthropods";
            case "DAMAGE_UNDEAD":
                return "Smite";
            case "DIG_SPEED":
                return "Efficiency";
            case "DURABILITY":
                return "Unbreaking";
            case "FIRE_ASPECT":
                return "Fire Aspect";
            case "KNOCKBACK":
                return "Knockback";
            case "LOOT_BONUS_BLOCKS":
                return "Fortune";
            case "LOOT_BONUS_MOBS":
                return "Looting";
            case "OXYGEN":
                return "Respiration";
            case "PROTECTION_ENVIRONMENTAL":
                return "Protection";
            case "PROTECTION_EXPLOSIONS":
                return "Blast Protection";
            case "PROTECTION_FALL":
                return "Feather Falling";
            case "PROTECTION_FIRE":
                return "Fire Protection";
            case "PROTECTION_PROJECTILE":
                return "Projectile Protection";
            case "SILK_TOUCH":
                return "Silk Touch";
            case "WATER_WORKER":
                return "Aqua Affinity";
            case "ARROW_FIRE":
                return "Flame";
            case "ARROW_DAMAGE":
                return "Power";
            case "ARROW_KNOCKBACK":
                return "Punch";
            case "ARROW_INFINITE":
                return "Infinity";
            case "MENDING":
                return "Mending";
            case "SWEEPING_EDGE":
                return "Sweeping Edge";
            case "VANISHING_CURSE":
                return "Curse of Vanishing";
            case "BINDING_CURSE":
                return "Curse of Binding";
            case "THORNS":
                return "Thorns";
            case "DEPTH_STRIDER":
                return "Depth Strider";
            case "FROST_WALKER":
                return "Frost Walker";
            case "LUCK":
                return "Luck of the Sea";
            case "LURE":
                return "Lure";
            case "LOYALTY":
                return "Loyalty";
            case "IMPALING":
                return "Impaling";
            case "RIPTIDE":
                return "Riptide";
            case "CHANNELING":
                return "Channeling";
            default:
                return "Unknown";
        }
    }

    public static String toRoman(int number) {
        if (number == 0) return "";

        int l = romanMap.floorKey(number);
        if (number == l) {
            return romanMap.get(number);
        }
        return romanMap.get(l) + toRoman(number - l);
    }

    public static void setDisplayName(ItemStack item, String name) {
        if (item == null || item.getItemMeta() == null) return;

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static void setDisplayName(ItemStack item, Component component) {
        if (item == null || item.getItemMeta() == null) return;

        ItemMeta meta = item.getItemMeta();
        meta.displayName(component);
        item.setItemMeta(meta);
    }

    public static List<ItemStack> stripItemsFromStack(List<ItemStack> items, Material... removeItems) {
        List<ItemStack> newList = new ArrayList<>(items);
        for (ItemStack item : items) {
            for (Material keep : removeItems) {
                if (item.getType() == keep) {
                    newList.remove(item);
                }
            }
        }
        return newList;
    }

    public static List<ItemStack> stripItemsFromStack(List<ItemStack> items, String... removeItems) {
        List<ItemStack> newList = new ArrayList<>(items);
        for (ItemStack item : items) {
            for (String displayName : removeItems) {
                if (hasDisplayName(item, displayName)) {
                    newList.remove(item);
                }
            }
        }
        return newList;
    }

    public static boolean containsLore(ItemStack item, String lore) {
        if (item.getItemMeta() != null && item.getItemMeta().getLore() != null) {
            for (String loreLine : item.getItemMeta().getLore()) {
                if (loreLine.contains(lore)) return true;
            }
        }

        return false;
    }

    public static void clearLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
    }

    public static List<String> getLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getLore() != null ? meta.getLore() : new ArrayList<>();
    }

    /**
     * Clear all lore lines starting at a given index to the end of the list.
     *
     * @param item
     * @param startingIndex
     */
    public static void clearLore(ItemStack item, int startingIndex) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.lore() != null ? meta.lore() : new ArrayList<>();
        if (startingIndex < 0 || startingIndex >= lore.size()) return;

        meta.lore(lore.subList(0, startingIndex));
        item.setItemMeta(meta);
    }

    public static void insertLore(ItemStack item, int index, String lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> currentLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        currentLore.add(index, lore);
        meta.setLore(currentLore);
        item.setItemMeta(meta);
    }

    public static void insertLore(ItemStack item, int index, Component lore) {
        ItemMeta meta = item.getItemMeta();
        List<Component> currentLore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        currentLore.add(index, lore);
        meta.lore(currentLore);
        item.setItemMeta(meta);
    }

    public static void insertLore(ItemStack item, int index, String... lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> currentLore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        for (int i = lore.length - 1; i >= 0; i--) {
            currentLore.add(index, lore[i]);
        }
        meta.setLore(currentLore);
        item.setItemMeta(meta);
    }

    public static void insertLore(ItemStack item, int index, Component... lore) {
        ItemMeta meta = item.getItemMeta();
        List<Component> currentLore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        for (int i = lore.length - 1; i >= 0; i--) {
            currentLore.add(index, lore[i]);
        }
        meta.lore(currentLore);
        item.setItemMeta(meta);
    }

    public static void addLore(ItemStack item, String... lore) {
        if (item == null || lore == null || lore.length == 0) return;

        ItemMeta meta = item.getItemMeta();
        List<String> currentLore = new ArrayList<>();
        try {
            if (meta != null && meta.getLore() != null) currentLore.addAll(meta.getLore());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.addAll(currentLore, lore);
        if (meta != null) {
            meta.setLore(currentLore);
            item.setItemMeta(meta);
        }
    }

    public static void addLoreComponents(ItemStack item, Component... lore) {
        if (item == null || lore == null || lore.length == 0) return;

        ItemMeta meta = item.getItemMeta();
        List<Component> currentLore = new ArrayList<>();
        try {
            if (meta != null && meta.hasLore()) currentLore.addAll(meta.lore());
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Component component : lore) {
            currentLore.add(component.decoration(TextDecoration.ITALIC, false));
        }

        if (meta != null) {
            meta.lore(currentLore);
            item.setItemMeta(meta);
        }
    }

    public static void addLore(ItemStack item, List<String> lore) {
        if (item == null || lore == null || lore.isEmpty()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> currentLore = new ArrayList<>();
        if (meta.getLore() != null) currentLore.addAll(meta.getLore());
        currentLore.addAll(lore);
        meta.setLore(currentLore);
        item.setItemMeta(meta);
    }

    public static void addLoreComponents(ItemStack item, List<Component> lore) {
        if (item == null || lore == null || lore.isEmpty()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> currentLore = new ArrayList<>();
        if (meta.hasLore()) currentLore.addAll(meta.lore());

        for (Component component : lore) {
            currentLore.add(component.decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(currentLore);
        item.setItemMeta(meta);
    }

    public static void setLore(ItemStack item, List<String> lore) {
        if (item == null || lore == null || lore.isEmpty()) return;

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static void setLoreComponents(ItemStack item, List<Component> lore) {
        if (item == null || lore == null || lore.isEmpty()) return;

        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Set an ItemStack to an amount between 1 and 99 (clamped). Will apply the MAX_STACK_SIZE DataComponent if needed.
     *
     * @param item
     * @param amount
     */
    public static void setItemAmount(ItemStack item, int amount) {
        int finalAmount = Mth.clamp(amount, 1, 99);
        if (finalAmount >= item.getType().getMaxStackSize()) {
            item.setData(DataComponentTypes.MAX_STACK_SIZE, finalAmount);
        }
        item.setAmount(finalAmount);
    }

    public static void setArmorColor(ItemStack i, Color color) {
        LeatherArmorMeta lam = (LeatherArmorMeta) i.getItemMeta();
        if (lam != null) {
            lam.setColor(color);
            i.setItemMeta(lam);
        }
    }

    public static void damageItem(ItemStack item, float percent) {
        if (item.getType().getMaxDurability() > 0) {
            short newDurability = (short) (item.getDurability() + (item.getType().getMaxDurability() * percent));
            item.setDurability(newDurability);
        }
    }

    public static Material getWool(ChatColor color) {
        switch (color) {

            case BLACK:
                return Material.BLACK_WOOL;
            case DARK_BLUE:
            case BLUE:
                return Material.BLUE_WOOL;
            case DARK_GREEN:
                return Material.GREEN_WOOL;
            case DARK_AQUA:
                return Material.CYAN_WOOL;
            case AQUA:
                return Material.LIGHT_BLUE_WOOL;
            case DARK_RED:
            case RED:
                return Material.RED_WOOL;
            case DARK_PURPLE:
                return Material.PURPLE_WOOL;
            case GOLD:
            case YELLOW:
                return Material.YELLOW_WOOL;
            case GRAY:
                return Material.LIGHT_GRAY_WOOL;
            case DARK_GRAY:
                return Material.GRAY_WOOL;
            case GREEN:
                return Material.LIME_WOOL;
            case LIGHT_PURPLE:
                return Material.MAGENTA_WOOL;
            case WHITE:
                return Material.WHITE_WOOL;
        }

        return Material.BLACK_WOOL;
    }

    public static Material getWool(DyeColor color) {
        if (color == null) return Material.BLACK_WOOL;

        if (color == WHITE) {
            return Material.WHITE_WOOL;
        } else if (color == ORANGE) {
            return Material.ORANGE_WOOL;
        } else if (color == MAGENTA) {
            return Material.MAGENTA_WOOL;
        } else if (color == LIGHT_BLUE) {
            return Material.LIGHT_BLUE_WOOL;
        } else if (color == YELLOW) {
            return Material.YELLOW_WOOL;
        } else if (color == LIME) {
            return Material.LIME_WOOL;
        } else if (color == PINK) {
            return Material.PINK_WOOL;
        } else if (color == GRAY) {
            return Material.GRAY_WOOL;
        } else if (color == LIGHT_GRAY) {
            return Material.LIGHT_GRAY_WOOL;
        } else if (color == CYAN) {
            return Material.CYAN_WOOL;
        } else if (color == PURPLE) {
            return Material.PURPLE_WOOL;
        } else if (color == BLUE) {
            return Material.BLUE_WOOL;
        } else if (color == BROWN) {
            return Material.BROWN_WOOL;
        } else if (color == GREEN) {
            return Material.GREEN_WOOL;
        } else if (color == RED) {
            return Material.RED_WOOL;
        } else if (color == BLACK) {
            return Material.BLACK_WOOL;
        }

        return Material.BLACK_WOOL;
    }

    public static Material getStainedGlass(ChatColor color) {
        switch (color) {

            case BLACK:
                return Material.BLACK_STAINED_GLASS;
            case DARK_BLUE:
            case BLUE:
                return Material.BLUE_STAINED_GLASS;
            case DARK_GREEN:
                return Material.GREEN_STAINED_GLASS;
            case DARK_AQUA:
                return Material.CYAN_STAINED_GLASS;
            case AQUA:
                return Material.LIGHT_BLUE_STAINED_GLASS;
            case DARK_RED:
            case RED:
                return Material.RED_STAINED_GLASS;
            case DARK_PURPLE:
                return Material.PURPLE_STAINED_GLASS;
            case GOLD:
            case YELLOW:
                return Material.YELLOW_STAINED_GLASS;
            case GRAY:
                return Material.LIGHT_GRAY_STAINED_GLASS;
            case DARK_GRAY:
                return Material.GRAY_STAINED_GLASS;
            case GREEN:
                return Material.LIME_STAINED_GLASS;
            case LIGHT_PURPLE:
                return Material.MAGENTA_STAINED_GLASS;
            case WHITE:
                return Material.WHITE_STAINED_GLASS;
        }

        return Material.BLACK_STAINED_GLASS;
    }

    public static Material getStainedGlass(DyeColor color) {
        if (color == null) return Material.BLACK_WOOL;

        if (color == WHITE) {
            return Material.WHITE_STAINED_GLASS;
        } else if (color == ORANGE) {
            return Material.ORANGE_STAINED_GLASS;
        } else if (color == MAGENTA) {
            return Material.MAGENTA_STAINED_GLASS;
        } else if (color == LIGHT_BLUE) {
            return Material.LIGHT_BLUE_STAINED_GLASS;
        } else if (color == YELLOW) {
            return Material.YELLOW_STAINED_GLASS;
        } else if (color == LIME) {
            return Material.LIME_STAINED_GLASS;
        } else if (color == PINK) {
            return Material.PINK_STAINED_GLASS;
        } else if (color == GRAY) {
            return Material.GRAY_STAINED_GLASS;
        } else if (color == LIGHT_GRAY) {
            return Material.LIGHT_GRAY_STAINED_GLASS;
        } else if (color == CYAN) {
            return Material.CYAN_STAINED_GLASS;
        } else if (color == PURPLE) {
            return Material.PURPLE_STAINED_GLASS;
        } else if (color == BLUE) {
            return Material.BLUE_STAINED_GLASS;
        } else if (color == BROWN) {
            return Material.BROWN_STAINED_GLASS;
        } else if (color == GREEN) {
            return Material.GREEN_STAINED_GLASS;
        } else if (color == RED) {
            return Material.RED_STAINED_GLASS;
        } else if (color == BLACK) {
            return Material.BLACK_STAINED_GLASS;
        }

        return Material.BLACK_STAINED_GLASS;
    }

    public static DyeColor getDyeColorFromChatColor(ChatColor color) {
        return getDyeColorFromChatColor(net.md_5.bungee.api.ChatColor.getByChar(color.getChar()));
    }

    public static DyeColor getDyeColorFromChatColor(net.md_5.bungee.api.ChatColor color) {
        if (net.md_5.bungee.api.ChatColor.BLACK.equals(color)) {
            return BLACK;
        } else if (net.md_5.bungee.api.ChatColor.DARK_BLUE.equals(color)) {
            return BLUE;
        } else if (net.md_5.bungee.api.ChatColor.DARK_GREEN.equals(color)) {
            return GREEN;
        } else if (net.md_5.bungee.api.ChatColor.DARK_AQUA.equals(color)) {
            return CYAN;
        } else if (net.md_5.bungee.api.ChatColor.DARK_RED.equals(color)) {
            return BROWN;
        } else if (net.md_5.bungee.api.ChatColor.DARK_PURPLE.equals(color)) {
            return PURPLE;
        } else if (net.md_5.bungee.api.ChatColor.GOLD.equals(color)) {
            return ORANGE;
        } else if (net.md_5.bungee.api.ChatColor.GRAY.equals(color)) {
            return LIGHT_GRAY;
        } else if (net.md_5.bungee.api.ChatColor.DARK_GRAY.equals(color)) {
            return GRAY;
        } else if (net.md_5.bungee.api.ChatColor.BLUE.equals(color)) {
            return LIGHT_BLUE;
        } else if (net.md_5.bungee.api.ChatColor.GREEN.equals(color)) {
            return LIME;
        } else if (net.md_5.bungee.api.ChatColor.AQUA.equals(color)) {
            return CYAN;
        } else if (net.md_5.bungee.api.ChatColor.RED.equals(color)) {
            return RED;
        } else if (net.md_5.bungee.api.ChatColor.LIGHT_PURPLE.equals(color)) {
            return MAGENTA;
        } else if (net.md_5.bungee.api.ChatColor.YELLOW.equals(color)) {
            return YELLOW;
        } else if (net.md_5.bungee.api.ChatColor.WHITE.equals(color)) {
            return WHITE;
        }
        return WHITE;
    }

    public static Color getColorFromChatColor(net.md_5.bungee.api.ChatColor color) {
        if (net.md_5.bungee.api.ChatColor.AQUA.equals(color)) {
            return Color.AQUA;
        } else if (net.md_5.bungee.api.ChatColor.BLACK.equals(color)) {
            return Color.BLACK;
        } else if (net.md_5.bungee.api.ChatColor.BLUE.equals(color)) {
            return Color.BLUE;
        } else if (net.md_5.bungee.api.ChatColor.DARK_AQUA.equals(color)) {
            return Color.BLUE;
        } else if (net.md_5.bungee.api.ChatColor.DARK_BLUE.equals(color)) {
            return Color.BLUE;
        } else if (net.md_5.bungee.api.ChatColor.DARK_GRAY.equals(color)) {
            return Color.GRAY;
        } else if (net.md_5.bungee.api.ChatColor.DARK_GREEN.equals(color)) {
            return Color.GREEN;
        } else if (net.md_5.bungee.api.ChatColor.DARK_PURPLE.equals(color)) {
            return Color.PURPLE;
        } else if (net.md_5.bungee.api.ChatColor.DARK_RED.equals(color)) {
            return Color.RED;
        } else if (net.md_5.bungee.api.ChatColor.GOLD.equals(color)) {
            return Color.YELLOW;
        } else if (net.md_5.bungee.api.ChatColor.GRAY.equals(color)) {
            return Color.GRAY;
        } else if (net.md_5.bungee.api.ChatColor.GREEN.equals(color)) {
            return Color.GREEN;
        } else if (net.md_5.bungee.api.ChatColor.LIGHT_PURPLE.equals(color)) {
            return Color.PURPLE;
        } else if (net.md_5.bungee.api.ChatColor.RED.equals(color)) {
            return Color.RED;
        } else if (net.md_5.bungee.api.ChatColor.WHITE.equals(color)) {
            return Color.WHITE;
        } else if (net.md_5.bungee.api.ChatColor.YELLOW.equals(color)) {
            return Color.YELLOW;
        }

        return null;
    }

    public static boolean hasNBTData(ItemStack item, String... data) {
        return Arrays.stream(data).anyMatch(tag -> hasNBTData(item, tag));
    }

    public static boolean hasNBTData(ItemStack item, String data) {
        if (item == null || item.getType() == Material.AIR) return false;

        net.minecraft.world.item.ItemStack dataItemStack = CraftItemStack.asNMSCopy(item);
        if (dataItemStack == null) return false;

        return getCompoundTag(item).contains(data);
    }

    public static boolean hasNBTData(CompoundTag comp, String data) {
        return comp != null && comp.contains(data);
    }

    public static boolean getNBTBoolean(ItemStack item, String data) {
        CompoundTag comp = getCompoundTag(item);
        return comp.contains(data) && comp.getBooleanOr(data, false);
    }

    public static UUID getNBTUUID(ItemStack item, String data) {
        CompoundTag comp = getCompoundTag(item);
        String uuidString = comp.contains(data) ? comp.getString(data).orElse(null) : null;
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

    public static String getNBTString(ItemStack item, String data) {
        CompoundTag comp = getCompoundTag(item);
        return comp.contains(data) ? comp.getString(data).orElse(null) : "";
    }

    public static String getNBTString(CompoundTag comp, String data) {
        return comp != null && comp.contains(data) ? comp.getString(data).orElse(null) : null;
    }

    public static int getNBTInt(ItemStack item, String data) {
        CompoundTag comp = getCompoundTag(item);
        return comp.contains(data) ? comp.getIntOr(data, 0) : 0;
    }

    public static long getNBTLong(ItemStack item, String data) {
        CompoundTag comp = getCompoundTag(item);
        return comp.contains(data) ? comp.getLongOr(data, 0L) : 0L;
    }

    public static ItemStack removeNBTTag(ItemStack item, String key) {
        CompoundTag comp = getCompoundTag(item);
        comp.remove(key);
        return item;
    }

    public static CompoundTag getCompoundTag(ItemStack item) {
        net.minecraft.world.item.ItemStack dataItemStack = CraftItemStack.asNMSCopy(item);
        return dataItemStack.getComponents().getOrDefault(CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
    }

    public static ItemStack getPotion(PotionType type, boolean splash, boolean extended, boolean upgraded) {
        return getPotion(new PotionData(type, extended, upgraded), splash);
    }

    public static ItemStack getPotion(PotionData data, boolean splash) {
        ItemStack potion = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionData(data);
        potion.setItemMeta(meta);
        return potion;
    }

    public static boolean isPotionType(ItemStack potion, PotionType type) {
        if (potion.getType() != Material.POTION && potion.getType() != Material.SPLASH_POTION) return false;

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        return meta.getBasePotionType() == type;
    }

    public static void clearInventory(Player p) {
        p.getInventory().clear();
        p.getInventory().setHelmet(new ItemStack(Material.AIR));
        p.getInventory().setBoots(new ItemStack(Material.AIR));
        p.getInventory().setChestplate(new ItemStack(Material.AIR));
        p.getInventory().setLeggings(new ItemStack(Material.AIR));
    }

    public static ItemStack getPlayerSkull(OfflinePlayer skullOwner, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(skullOwner);
        skullMeta.setDisplayName(ChatColor.RESET + displayName);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static ItemStack getPlayerSkull(PlayerProfile profile, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setPlayerProfile(profile);
        skullMeta.setDisplayName(ChatColor.RESET + displayName);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static UUID getSkullOwner(Skull headMeta) {
        if (headMeta == null) return null;

        OfflinePlayer owningPlayer = headMeta.getOwningPlayer();
        if (owningPlayer != null) {
            return owningPlayer.getUniqueId();
        } else {
            PlayerProfile profile = headMeta.getPlayerProfile();
            if (profile != null) {
                return profile.getId();
            }
        }

        return null;
    }

    public static void setSkull(Block b, PlayerProfile profile) {
        setSkull(b, profile, null);
    }

    public static void setSkull(Block b, PlayerProfile profile, Runnable onComplete) {
        Material head = profile != null ? Material.PLAYER_HEAD : Material.SKELETON_SKULL;
        Rotatable data = ((Rotatable) head.createBlockData());
        if (b.getBlockData() instanceof Rotatable) {
            data.setRotation(((Rotatable) b.getBlockData()).getRotation());
        }
        b.setBlockData(data);

        if (profile != null) {
            Skull skullState = (Skull) b.getState();
            CompletableFuture.runAsync(() -> skullState.setPlayerProfile(profile))
                    .thenRun(() -> {
                        skullState.update(true, false);

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
        }
    }

    public static ItemStack getMendingBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(Enchantment.MENDING, 1, true);
        book.setItemMeta(meta);
        return book;
    }

    public static List<ItemStack> splitStack(final ItemStack itemStack) {
        return splitStack(itemStack, itemStack.getAmount());
    }

    public static List<ItemStack> splitStack(final ItemStack itemStack, int amount) {
        if (amount <= 0) return new ArrayList<>();

        int maxStackSize = itemStack.getType().getMaxStackSize();

        if (amount > maxStackSize) {
            List<ItemStack> items = new ArrayList<>();
            while (amount > 0) {
                ItemStack splitItem = new ItemStack(itemStack);
                splitItem.setAmount(Math.min(amount, maxStackSize));

                items.add(splitItem);
                amount -= splitItem.getAmount();
            }
            return items;
        } else {
            ItemStack singleStack = itemStack.clone();
            singleStack.setAmount(amount);
            return Collections.singletonList(singleStack);
        }
    }

    /**
     * Given a list of itemStacks, collapse them down to the minimum number of itemStacks that respect maxStackSize
     *
     * @param items
     * @return
     */
    public static List<ItemStack> collapseItems(List<ItemStack> items) {
        if (items == null) return new ArrayList<>();

        Map<ItemStack, Integer> itemMap = new HashMap<>();
        for (ItemStack item : items) {
            ItemStack singleItemStack = item.asOne();
            if (itemMap.containsKey(singleItemStack)) {
                itemMap.put(singleItemStack, itemMap.get(singleItemStack) + item.getAmount());
            } else {
                itemMap.put(singleItemStack, item.getAmount());
            }
        }

        List<ItemStack> collapsed = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : itemMap.entrySet()) {
            collapsed.addAll(splitStack(entry.getKey(), entry.getValue()));
        }

        return collapsed;
    }

    public static ItemStack getColoredLeatherArmor(Material m, Color color) {
        ItemStack item = new ItemStack(m);
        LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
        lam.setColor(color);
        item.setItemMeta(lam);
        return item;
    }

    public static ItemStack getShieldFromBanner(ItemStack banner) {
        return getShieldFromBanner(null, banner);
    }

    public static ItemStack getShieldFromBanner(ItemStack sourceItem, ItemStack banner) {
        ItemStack shield = sourceItem != null ? sourceItem : new ItemStack(Material.SHIELD);
        if (banner == null) return shield;

        ItemMeta meta = shield.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockStateMeta)) {
            return shield;
        }

        if (!(blockStateMeta.getBlockState() instanceof Banner bannerCopy)) {
            return shield;
        }
        
        bannerCopy.setBaseColor(getBannerBaseColor(banner));

        ItemMeta bannerItemMeta = banner.getItemMeta();
        if (bannerItemMeta instanceof BannerMeta bannerMeta) {
            bannerCopy.setPatterns(bannerMeta.getPatterns());
        }
        
        bannerCopy.update();
        blockStateMeta.setBlockState(bannerCopy);
        shield.setItemMeta(blockStateMeta);
        return shield;
    }

    public static DyeColor getBannerBaseColor(ItemStack banner) {
        if (banner == null || !banner.getType().name().endsWith("_BANNER")) {
            return BLACK;
        }

        String materialName = banner.getType().name();
        String colorName = materialName.replace("_BANNER", "");

        try {
            return DyeColor.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            return BLACK;
        }
    }

    public static void clearEnchants(ItemStack itemStack) {
        for (Enchantment enchantment : new ArrayList<>(itemStack.getEnchantments().keySet())) {
            itemStack.removeEnchantment(enchantment);
        }
    }

    public static void applyRandomEnchantment(ItemStack itemStack) {
        try {
            List<Enchantment> enchants = getPossibleEnchantmentsForItem(itemStack);
            if (enchants.isEmpty()) return;

            Enchantment enchantment = enchants.get(ThreadLocalRandom.current().nextInt(enchants.size()));
            int level = new Random().nextInt(enchantment.getMaxLevel()) + 1;
            addEnchantmentToItem(itemStack, enchantment, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<Enchantment, Integer> getEnchantmentsOnItem(ItemStack itemStack) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            return esm.getStoredEnchants();
        } else {
            return itemStack.getEnchantments();
        }
    }

    public static List<Enchantment> getPossibleEnchantmentsForItem(ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            return Arrays.stream(Enchantment.values())
                    .sorted(Comparator.comparing(ItemStackUtil::getEnchantmentName))
                    .collect(Collectors.toList());
        }

        return Arrays.stream(Enchantment.values())
                .filter(enchantment -> {
                    if (!enchantment.canEnchantItem(item)) return false;

                    return item.getEnchantments().keySet().stream().noneMatch(ench -> ench.conflictsWith(enchantment));
                })
                .sorted(Comparator.comparing(ItemStackUtil::getEnchantmentName))
                .collect(Collectors.toList());
    }


    public static void addEnchantmentToItem(ItemStack itemStack, Enchantment enchantment, int level) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            esm.addStoredEnchant(enchantment, level, true);
            itemStack.setItemMeta(esm);
        } else {
            itemStack.addEnchantment(enchantment, level);
        }
    }

    public static void removeItemEnchantment(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            esm.removeStoredEnchant(enchantment);
            itemStack.setItemMeta(esm);
        } else {
            itemStack.removeEnchantment(enchantment);
        }
    }

    public static void copyEnchants(ItemStack itemStack, ItemStack receiver) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta newEsm = (EnchantmentStorageMeta) receiver.getItemMeta();
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            esm.getStoredEnchants().forEach((key, value) -> newEsm.addStoredEnchant(key, value, true));
            receiver.setItemMeta(newEsm);
        } else {
            itemStack.getEnchantments().forEach(receiver::addEnchantment);
        }
    }

    public static int getEnchantLevel(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            return esm.getStoredEnchantLevel(enchantment);
        } else {
            return itemStack.getEnchantmentLevel(enchantment);
        }
    }

    public static boolean canAddEnchant(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            return esm.getStoredEnchants().keySet().stream().noneMatch(e -> e.conflictsWith(enchantment));
        } else {
            return itemStack.getEnchantments().keySet().stream().noneMatch(e -> e.conflictsWith(enchantment));
        }
    }

    public static boolean hasEnchant(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            return esm.getStoredEnchants().containsKey(enchantment);
        } else {
            return itemStack.getEnchantments().containsKey(enchantment);
        }
    }

    public static String getPotionString(ItemStack item) {
        return getPotionString(item, false);
    }

    public static String getPotionString(ItemStack item, boolean withExtended) {
        Material type = item.getType();
        if (item.getItemMeta() == null || (type != Material.POTION && type != Material.SPLASH_POTION))
            return getFriendlyName(item);

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionType basePotionType = meta.getBasePotionType();
        if (basePotionType == null) {
            return getFriendlyName(item);
        }

        return (withExtended && basePotionType.isExtendable() ? "Extended " : "")
                + getFriendlyName(item) + " "
                + (basePotionType.isUpgradeable() ? "2" : "1");
    }

    public static String getPotionString(ThrownPotion potion) {
        Iterator<PotionEffect> iterator = potion.getEffects().iterator();
        if (iterator.hasNext()) {
            int level = iterator.next().getAmplifier() + 1;
            return getFriendlyName(potion.getItem()) + " " + level;
        } else {
            return getFriendlyName(potion.getItem());
        }
    }

    public static Map.Entry<Enchantment, Integer> getFirstEnchantment(ItemStack item, boolean stored) {
        EnchantmentStorageMeta esm = (EnchantmentStorageMeta) item.getItemMeta();
        if (stored) {
            if (esm.getStoredEnchants().isEmpty()) return null;
            return esm.getStoredEnchants().entrySet().iterator().next();
        } else {
            if (esm.getEnchants().isEmpty()) return null;
            return esm.getEnchants().entrySet().iterator().next();
        }
    }
}
