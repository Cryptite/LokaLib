package com.lokamc.utils;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftShulkerBox;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.inventory.CraftMetaBlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.lokamc.utils.ItemStackUtil.getNBTString;

public class InventoryUtil {
    public static boolean clickInvolvedItem(InventoryClickEvent e, Material material) {
        return testClickedItem(e, itemStack -> itemStack.getType() == material);
    }

    public static boolean testClickedItem(InventoryClickEvent e, Predicate<ItemStack> predicate) {
        ItemStack clicked = null;
        InventoryAction action = e.getAction();
        if (action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.HOTBAR_SWAP) {
            Inventory clickedInventory = e.getClickedInventory();
            if (e.getHotbarButton() == -1) {
                clicked = e.getWhoClicked().getInventory().getItemInOffHand();
                if (clickedInventory != null) {
                    clicked = clickedInventory.getItem(e.getSlot());
                }
            } else {
                if (clickedInventory != null) {
                    clicked = clickedInventory.getItem(e.getSlot());
                    if (clicked == null) {
                        clicked = clickedInventory.getItem(e.getHotbarButton());
                    }
                }
            }
        } else {
            clicked = e.getCurrentItem();
        }

        if (clicked == null || clicked.getType() == Material.AIR) {
            clicked = e.getCursor();
        }

        return clicked != null && predicate.test(clicked);
    }

    public static boolean testInventory(Player p, Predicate<ItemStack> predicate) {
        for (ItemStack itemStack : p.getInventory()) {
            if (itemStack == null) continue;

            if (predicate.test(itemStack)) {
                return true;
            } else if (itemStack.getItemMeta() instanceof CraftMetaBlockState state
                    && state.getBlockState() instanceof CraftShulkerBox box) {
                for (ItemStack shulkerItem : box.getInventory()) {
                    if (shulkerItem != null && predicate.test(shulkerItem)) {
                        return true;
                    }
                }
            }
        }

        if (predicate.test(p.getItemOnCursor())) {
            return true;
        }

        for (ItemStack itemStack : p.getOpenInventory().getTopInventory()) {
            if (itemStack != null && predicate.test(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canFitInInventory(Player p, ItemStack... contents) {
        ServerLevel serverLevel = ((CraftWorld) p.getWorld()).getHandle();
        ServerPlayer fakePlayer = new ServerPlayer(MinecraftServer.getServer(), serverLevel, new GameProfile(p.getUniqueId(), p.getName()), ClientInformation.createDefault());
        return canFitInInventory(p, fakePlayer, contents);
    }

    public static boolean canFitInInventory(Player p, ServerPlayer fakePlayer, ItemStack... contents) {
        CraftInventoryPlayer inv = new CraftInventoryPlayer(fakePlayer.getInventory());
        inv.setContents(p.getInventory().getContents());
        List<ItemStack> clonedContents = Arrays.stream(contents).map(ItemStack::clone).toList();
        return inv.addItem(clonedContents.toArray(new ItemStack[0])).isEmpty();
    }

    public static boolean canFitInInventory(Inventory inv, ItemStack item) {
        return canFitInInventory(inv, item, true);
    }

    public static boolean canFitInInventory(Inventory inv, ItemStack item, boolean andClone) {
        Inventory checkInv = inv;

        if (andClone) {
            checkInv = Bukkit.createInventory(null, InventoryType.PLAYER);
            checkInv.setContents(inv.getContents());
        }

        return checkInv.addItem(item.clone()).isEmpty();
    }

    public static Inventory cloneInventory(Inventory inv) {
        Inventory cloned;
        if (inv.getType() == InventoryType.PLAYER) {
            cloned = Bukkit.createInventory(null, InventoryType.PLAYER);
        } else {
            cloned = Bukkit.createInventory(null, inv.getSize());
        }
        cloned.setContents(inv.getContents());
        return cloned;
    }

    public static Inventory clonePlayerInventoryContents(PlayerInventory inv) {
        Inventory cloned = Bukkit.createInventory(null, 36);

        //This preserves item order in case of nulls
        List<ItemStack> clonedContents = Arrays.stream(inv.getStorageContents())
                .map(itemStack -> itemStack != null ? itemStack.clone() : null)
                .toList();
        cloned.setContents(clonedContents.toArray(new ItemStack[0]));
        return cloned;
    }

    public static boolean stripShulkerBoxes(Player p, Inventory inv) {
        return stripItems(p, inv, item -> Tag.SHULKER_BOXES.isTagged(item.getType()));
    }

    public static void updateShulkerInventory(ItemStack item, Inventory inventory) {
        if (item.getItemMeta() instanceof BlockStateMeta im
                && im.getBlockState() instanceof ShulkerBox shulker) {
            im.setBlockState(shulker);
            item.setItemMeta(im);
        }
    }

    public static boolean isEmptyInventory(Inventory i) {
        return Arrays.stream(i.getContents()).noneMatch(Objects::nonNull);
    }

    public static boolean stripItems(Player p, Inventory inv, Predicate<ItemStack> predicate) {
        boolean hadInvalidItems = false;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            if (predicate.test(item)) {
                if (PlayerUtil.hasFullInventory(p)) {
                    p.getWorld().dropItem(p.getLocation(), item);
                } else {
                    p.getInventory().addItem(item);
                }
                inv.setItem(i, null);
                hadInvalidItems = true;
            }
        }

        return hadInvalidItems;
    }

    public static int removeAllFromInventory(Player p, ItemStack item) {
        int totalStacksRemoved = 0;
        while (removeItemFromInventory(p, item)) {
            totalStacksRemoved++;
        }
        return totalStacksRemoved;
    }

    /**
     * @return Whether this succeeded in taking the item out of the inventory. If there was not enough of the item in the inventory, this returns false
     */
    public static boolean removeItemFromInventory(Player p, ItemStack item) {
        PlayerInventory inventory = p.getInventory();
        ItemStack[] contents = inventory.getContents();
        if (removeItemFromContents(contents, item)) {
            inventory.setContents(contents);
            return true;
        }

        //TODO: Test .setArmorContents after this
        ItemStack[] armorContents = inventory.getArmorContents();
        if (removeItemFromContents(armorContents, item)) {
            inventory.setArmorContents(armorContents);
            return true;
        }

        ItemStack mainHand = inventory.getItemInMainHand();
        if (item.equals(mainHand)) {
            inventory.setItemInMainHand(null);
            return true;
        } else if (item.isSimilar(mainHand) && item.getAmount() < mainHand.getAmount()) {
            mainHand.setAmount(mainHand.getAmount() - item.getAmount());
            return true;
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (item.equals(offHand)) {
            inventory.setItemInOffHand(null);
            return true;
        } else if (item.isSimilar(offHand) && item.getAmount() < offHand.getAmount()) {
            offHand.setAmount(offHand.getAmount() - item.getAmount());
            return true;
        }

        return false;
    }

    public static boolean removeItemFromContents(ItemStack[] contents, ItemStack toRemove) {
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (toRemove.equals(itemStack)) {
                contents[i] = null;
                return true;
            } else if (toRemove.isSimilar(itemStack) && toRemove.getAmount() < itemStack.getAmount()) {
                itemStack.setAmount(itemStack.getAmount() - toRemove.getAmount());
                return true;
            }
        }

        return false;
    }

    public static ItemStack[] getObfuscatedShulkerInventory(ItemStack itemStack) {
        if (itemStack.getItemMeta() instanceof CraftMetaBlockState state
                && state.getBlockState() instanceof CraftShulkerBox box) {

            String shulkerinv = getNBTString(itemStack, "shulkerinv");
            if (!Strings.isNullOrEmpty(shulkerinv)) {
                try {
                    return SerializeInventory.fromBase64ToContents(shulkerinv);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            return box.getInventory().getContents();
        }

        return new ItemStack[0];
    }

    public static Inventory getShulkerInventory(ItemStack item) {
        if (item.getItemMeta() instanceof BlockStateMeta blockStateMeta
                && blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
            return shulkerBox.getInventory();
        }

        return Bukkit.createInventory(null, 27, "Shulker Box");
    }

    public static boolean isEmptyShulker(ItemStack item) {
        if (item.getItemMeta() instanceof BlockStateMeta blockStateMeta
                && blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
            return isEmptyInventory(shulkerBox.getInventory());
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

    /**
     * @return Take a given amount of an itemstack from an inventory, returning false if it could not remove the amount given.
     */
    public static boolean takeFromInventory(Inventory inv, ItemStack itemStack, int amount) {
        int taken = amount;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (!itemStack.equals(item)) continue;

            if (item.getAmount() > taken) {
                item.setAmount(item.getAmount() - taken);
                return true;
            } else {
                taken -= item.getAmount();
                inv.setItem(i, null);
            }

            if (taken <= 0) {
                return true;
            }
        }

        return false;
    }
}
