package com.lokamc.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftItemFrame;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

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

    public static CompletableFuture<ItemFrame> getItemFrame(Plugin plugin, Block b) {
        return getItemFrame(plugin, b.getLocation());
    }

    public static CompletableFuture<ItemFrame> getItemFrame(Plugin plugin, Location l) {
        return l.getWorld().getChunkAtAsync(l).thenApplyAsync(chunk -> {
            try {
                for (Entity entity : l.getNearbyEntities(2, 2, 2)) {
                    if (entity instanceof CraftItemFrame) {
                        ItemFrame frame = (ItemFrame) entity;
                        Block frameBlock = frame.getLocation().getBlock();
                        if (frameBlock.equals(l.getBlock()) || frameBlock.getRelative(frame.getFacing().getOppositeFace()).equals(l.getBlock())) {
                            return frame;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
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

    public static CompletableFuture<ItemFrame> getItemFrame(Plugin plugin, Location l, Block against) {
        return l.getWorld().getChunkAtAsync(l).thenApplyAsync(chunk -> {
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
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

    public static int toRelativeCoordinateInChunk(Location l) {
        return toRelativeCoordinateInChunk(l.getBlock());
    }

    public static int toRelativeCoordinateInChunk(Block b) {
        final int relX = (b.getX() % 16 + 16) % 16;
        final int relZ = (b.getZ() % 16 + 16) % 16;
        return (b.getY() & 0xFFFF) | ((relX & 0xFF) << 16) | ((relZ & 0xFF) << 24);
    }
}
