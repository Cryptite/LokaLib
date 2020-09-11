package com.lokamc.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftItemFrame;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockUtil {
    public static BlockFace[] surroundingBlockFaces = new BlockFace[]{
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST
    };

    public static BlockFace[] adjacentBlockFaces = new BlockFace[]{
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN
    };

    public static int getAngleFromData(byte data) {
        switch (data) {
            case 2:
                return 0;
            case 3:
                return 180;
            case 4:
                return 270;
            default:
                return 90;
        }
    }

    public static int rotate180(int angle) {
        if (angle == 0) {
            return 180;
        } else if (angle == 90) {
            return 270;
        } else if (angle == 180) {
            return 0;
        } else if (angle == 270) {
            return 90;
        }

        return 0;
    }

    public static void clearSign(Block b) {
        Sign sign = (Sign) b.getState();
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update();
    }

    public static ItemFrame getItemFrame(Block b) {
        return getItemFrame(b.getLocation());
    }

    public static ItemFrame getItemFrame(Location l) {
        l.getChunk().load();
        for (Entity entity : l.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof CraftItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                Block frameBlock = frame.getLocation().getBlock();
                if (frameBlock.equals(l.getBlock()) || frameBlock.getRelative(frame.getFacing().getOppositeFace()).equals(l.getBlock())) {
                    return frame;
                }
            }
        }

        return null;
    }

    public static ItemFrame getItemFrame(Location l, Block against) {
        l.getChunk().load();
        for (Entity entity : l.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof CraftItemFrame) {
                ItemFrame frame = (ItemFrame) entity;
                Block frameBlock = frame.getLocation().getBlock();
                if (frameBlock.equals(l.getBlock()) || frameBlock.getRelative(frame.getFacing().getOppositeFace()).equals(l.getBlock())) {
                    if (getBlockHangingAgainst(frame).equals(against)) {
                        return frame;
                    }
                }
            }
        }

        return null;
    }

    public static Block getBlockHangingAgainst(Hanging hanging) {
        return hanging.getLocation().getBlock().getRelative(hanging.getFacing().getOppositeFace());
    }

    public static void dropItemTypeFromBlock(Player p, Block b, Material type) {
        dropItemTypeFromBlock(p, b, b.getLocation(), type);
    }

    public static void dropItemTypeFromBlock(Player p, Block b, Location l, Material type) {
        ItemStack holding = p.getInventory().getItemInMainHand().clone();
        net.minecraft.server.v1_16_R2.World world = ((CraftWorld) b.getWorld()).getHandle();
        net.minecraft.server.v1_16_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(holding);

        net.minecraft.server.v1_16_R2.Block.dropItems(CraftMagicNumbers.getBlock(type).getBlockData(),
                world,
                new net.minecraft.server.v1_16_R2.BlockPosition(l.getX(), l.getY(), l.getZ()),
                null, ((CraftPlayer) p).getHandle(), nmsItem);
    }
}
