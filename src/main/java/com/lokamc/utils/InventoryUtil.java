package com.lokamc.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

public class InventoryUtil {
    public static boolean stripShulkerBoxes(Player p, Inventory inv) {
        boolean hadBox = false;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            if (item.getType().toString().contains("SHULKER_BOX")) {
                if (PlayerUtil.hasFullInventory(p)) {
                    p.getWorld().dropItem(p.getLocation(), item);
                } else {
                    p.getInventory().addItem(item);
                }
                inv.setItem(i, null);
                hadBox = true;
            }
        }

        return hadBox;
    }

    public static Inventory getShulkerInventory(ItemStack item) {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
            if (im.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                return shulker.getInventory();
            }
        }

        return Bukkit.createInventory(null, 27, "Shulker Box");
    }

    public static boolean isEmptyInventory(Inventory i) {
        for (ItemStack item : i.getContents()) {
            if (item != null)
                return false;
        }
        return true;
    }

    public static int getInventorySize(int amount) {
        if (amount >= 45) {
            return 54;
        } else if (amount >= 36) {
            return 45;
        } else if (amount >= 27) {
            return 36;
        } else if (amount >= 18) {
            return 27;
        } else if (amount >= 9) {
            return 18;
        }
        return 9;
    }

    public static int getInventoryEmptySlots(Player p) {
        int count = 0;
        for (ItemStack item : p.getInventory().getStorageContents()) {
            if (item == null) count++;
        }
        return count;
    }

    public static boolean hasItemInInventory(Player p, ItemStack itemStack) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.isSimilar(itemStack)) return true;
        }

        return false;
    }

    public static boolean removeItemFromInventory(Player p, ItemStack item) {
        PlayerInventory inventory = p.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (item.equals(itemStack)) {
                inventory.setItem(i, null);
                p.updateInventory();
                return true;
            } else if (item.isSimilar(itemStack) && item.getAmount() < itemStack.getAmount()) {
                itemStack.setAmount(itemStack.getAmount() - item.getAmount());
                p.updateInventory();
                return true;
            }
        }

        ItemStack[] armorContents = inventory.getArmorContents();
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack itemStack = armorContents[i];
            if (item.equals(itemStack)) {
                armorContents[i] = null;
                inventory.setArmorContents(armorContents);
                p.updateInventory();
                return true;
            } else if (item.isSimilar(itemStack) && item.getAmount() < itemStack.getAmount()) {
                itemStack.setAmount(itemStack.getAmount() - item.getAmount());
                p.updateInventory();
                return true;
            }
        }

        ItemStack mainHand = inventory.getItemInMainHand();
        if (item.equals(mainHand)) {
            inventory.setItemInMainHand(null);
            p.updateInventory();
            return true;
        } else if (item.isSimilar(mainHand) && item.getAmount() < mainHand.getAmount()) {
            mainHand.setAmount(mainHand.getAmount() - item.getAmount());
            p.updateInventory();
            return true;
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (item.equals(offHand)) {
            inventory.setItemInOffHand(null);
            p.updateInventory();
            return true;
        } else if (item.isSimilar(offHand) && item.getAmount() < offHand.getAmount()) {
            offHand.setAmount(offHand.getAmount() - item.getAmount());
            p.updateInventory();
            return true;
        }


        return false;
    }
}
