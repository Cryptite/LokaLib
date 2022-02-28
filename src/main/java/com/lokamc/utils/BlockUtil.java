package com.lokamc.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftItemFrame;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;

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

    public static ItemFrame getItemFrameExact(Location l) {
        l.getChunk().load();
        for (Entity entity : l.getNearbyEntities(1, 1, 1)) {
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
}
