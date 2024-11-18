package com.lokamc.types;

import com.google.common.math.IntMath;
import com.lokamc.utils.ItemStackUtil;
import com.lokamc.utils.LocationUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.block.impl.CraftFluids;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.lokamc.utils.BlockUtil.adjacentBlockFaces;
import static com.lokamc.utils.BlockUtil.surroundingBlockFaces;
import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;
import static com.sk89q.worldedit.bukkit.BukkitAdapter.asBlockVector;

public class StringBlock extends StringLocation implements Comparable<StringBlock> {
    public int priority = 1;
    private Boolean doBlockUpdate;
    private boolean doLightUpdate;
    private Block block;
    private BlockData blockData;
    //    private BlockState blockState;
    private BlockStateHolder blockStateHolder;
    private Runnable onSetRunnable;
    private boolean empty;

    public StringBlock(Location l) {
        super(l);
    }

    public StringBlock(StringLocation l) {
        super(l);
    }

    public StringBlock(StringLocation l, Material material) {
        super(l);
        this.blockData = material.createBlockData();
    }

    public StringBlock(Block b) {
        super(b);
        this.block = b;
//        this.blockState = b.getState();
        this.blockData = b.getBlockData();
    }

    public StringBlock(Block b, Material material) {
        this(b, material.createBlockData());
    }

    public StringBlock(Block b, BlockData blockData) {
        super(b);
        this.block = b;
//        this.blockState = b.getState();
        this.blockData = blockData;
    }

    public StringBlock(Location l, int priority) {
        super(l);
        Block b = l.getBlock();
        this.block = b;
        this.blockData = b.getBlockData();
//        this.blockState = b.getState();
        this.priority = priority;
    }

    public StringBlock(Block b, int priority) {
        super(b);
        this.block = b;
        this.blockData = b.getBlockData();
//        this.blockState = b.getState();
        this.priority = priority;
    }

    public StringBlock(String args) {
        super(StringLocation.fromArgs(args));
    }

    public StringBlock(StringBlock block) {
        super(block);
        blockData = block.getBlockData();
    }

    public StringBlock(StringBlock block, Material material) {
        super(block);
        blockData = material.createBlockData();
    }

    public StringBlock(World world, int x, int y, int z) {
        super(world, x, y, z);
    }

    public StringBlock(World world, int x, int y, int z, Material material) {
        super(world, x, y, z);
        blockData = material.createBlockData();
    }

    public StringBlock(World world, int x, int y, int z, BlockData blockData) {
        super(world, x, y, z);
        this.blockData = blockData;
    }

    public StringBlock(World world, int x, int y, int z, ChunkSnapshot c) {
        super(world, x, y, z);

        int snapshotBlockX = IntMath.mod(getBlockX(), 16);
        int snapshotBlockZ = IntMath.mod(getBlockZ(), 16);

//        if ((x / 16) != c.getX() || (z / 16) != c.getZ()) {
//            c = ChunkSnapshotCache.getInstance().getChunkSnapshot(new StringChunk(Bukkit.getWorld(c.getWorldName()), x >> 4, z >> 4));
//        }

        blockData = c.getBlockData(snapshotBlockX, y, snapshotBlockZ);
    }

    public ItemStack getItemStack() {
        return new ItemStack(blockData.getMaterial());
    }

    public void addOnSetConsumer(Runnable runnable) {
        this.onSetRunnable = runnable;
    }

    public Runnable getOnSetRunnable() {
        return onSetRunnable;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setType(Material m) {
        blockData = m.createBlockData();
    }

    public void setBlockTypeAsync(Material m) {
        setBlockTypeAsync(m, null);
    }

    public void setBlockTypeAsync(Material m, Runnable runnable) {
        setBlockTypeAsync(m, true, runnable);
    }

    public void setBlockTypeAsync(Material m, boolean update, Runnable runnable) {
        getWorld().getChunkAtAsync(this)
                .thenRun(() -> {
                    setBlockType(m, update);
                    if (runnable != null) {
                        runnable.run();
                    }
                });
    }

    public void setBlockType(Material m) {
        if (block == null) block = getBlock();
        block.setType(m);
    }

    public void setBlockType(Material m, boolean notify) {
        if (block == null) block = getBlock();
        block.setType(m, notify);
    }

    public void setData(BlockData data) {
        this.blockData = data;
    }

    public void setBlock(BlockData blockData) {
        setBlock(blockData, false);
    }

    public void setBlock(BlockData blockData, boolean update) {
        if (block == null) block = getBlock();

        if (!statesMatch(block.getBlockData(), blockData)) {
            block.setBlockData(blockData, update); //TODO: Reimplement, doLightUpdate);
        }
    }

    public void setBlockAsync(BlockData blockData) {
        setBlockAsync(blockData, false);
    }

    public void setBlockAsync(BlockData blockData, boolean update) {
        getWorld().getChunkAtAsync(this)
                .thenRun(() -> {
                    if (block == null) block = getBlock();

                    if (!statesMatch(block.getBlockData(), blockData)) {
                        block.setBlockData(blockData, update); //TODO: Reimplement, doLightUpdate);
                    }
                });
    }

    private boolean statesMatch(BlockData b1, BlockData b2) {
        if (b1.matches(b2)) {
            if (b1 instanceof CraftFluids f1 && b2 instanceof CraftFluids f2) {
                return f1.getLevel() == f2.getLevel();
            }

            return true;
        }

        return false;
    }

    public void setTypeAndData(Material m) {
        setTypeAndData(m, true);
    }

    public void setTypeAndData(Material m, boolean applyPhysics) {
        if (block == null) block = getBlock();
        block.setType(m, applyPhysics);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        block = getBlock();
    }

    @Override
    public void setX(double x) {
        super.setX(x);
        block = getBlock();
    }

    @Override
    public void setY(double y) {
        super.setY(y);
        block = getBlock();
    }

    @Override
    public void setZ(double z) {
        super.setZ(z);
        block = getBlock();
    }

    public Material getType() {
        if (blockData == null) {
            blockData = getBlockData();
        }

        return blockData.getMaterial();
    }

    public Material getBlockType() {
        return getBlock().getType();
    }

    public boolean hasBlockTypeInDirection(ChunkSnapshot c, BlockFace face, Material type, int amount) {
        int checks = 0;
        while (checks++ < amount) {
            if (getRelativeBlock(face, checks, c).getType() != type) return false;
        }

        return true;
    }

    public StringBlock getRelativeBlock(BlockFace blockFace, ChunkSnapshot c) {
        return getRelativeBlock(blockFace, 1, c);
    }

    public StringBlock getRelativeBlock(BlockFace face, int amount, ChunkSnapshot c) {
        int xOffset = face.getModX() * amount;
        int yOffset = face.getModY() * amount;
        int zOffset = face.getModZ() * amount;
        return new StringBlock(getWorld(), getBlockX() + xOffset, getBlockY() + yOffset, getBlockZ() + zOffset, c);
    }

    public StringBlock getFirstSolidBlockDown(ChunkSnapshot snapshot, int maxDistance) {
        Material type = getRelativeBlock(BlockFace.DOWN, snapshot).getType();
        int distance = 1;
        int currentY = getBlockY() - 1;
        while (!type.isSolid()) {
            type = getRelativeBlock(BlockFace.DOWN, distance, snapshot).getType();

            if (maxDistance > 0 && distance++ >= maxDistance) return null;
            if (currentY-- < 5) return null;
        }

        return new StringBlock(getWorld(), getBlockX(), currentY + 1, getBlockZ(), snapshot);
    }

    public StringBlock getSurface(ChunkSnapshot snapshot) {
        return getSurface(snapshot, -1);
    }

    public StringBlock getSurface(ChunkSnapshot snapshot, int maxLightLevel) {
        int x = IntMath.mod(getBlockX(), 16);
        int z = IntMath.mod(getBlockZ(), 16);

        int currentY = Math.min(252, getBlockY());

        while (true) {
            Material below = snapshot.getBlockData(x, currentY - 1, z).getMaterial();
            Material type = snapshot.getBlockData(x, currentY, z).getMaterial();
            Material above = snapshot.getBlockData(x, currentY + 1, z).getMaterial();
            int blockLightLevel = snapshot.getBlockSkyLight(x, currentY, z);
            if (below.isSolid()
                    && type == Material.AIR
                    && above == Material.AIR
                    && (maxLightLevel == -1 || blockLightLevel <= maxLightLevel)) {
                break;
            } else if (currentY < 5) {
                return null;
            }

            currentY--;
        }

        return new StringBlock(getWorld(), getBlockX(), currentY, getBlockZ(), snapshot);
    }

    public StringBlock getRandomAdjacentBlock(ChunkSnapshot snapshot, boolean includeVertical) {
        BlockFace direction;
        if (includeVertical) {
            direction = adjacentBlockFaces[ThreadLocalRandom.current().nextInt(adjacentBlockFaces.length)];
        } else {
            direction = surroundingBlockFaces[ThreadLocalRandom.current().nextInt(surroundingBlockFaces.length)];
        }
        return getRelativeBlock(direction, snapshot);
    }

    public void getRandomAdjacentBlock(ChunkSnapshot snapshot, boolean includeVertical, BiConsumer<StringBlock, BlockFace> consumer) {
        BlockFace direction;
        if (includeVertical) {
            direction = adjacentBlockFaces[ThreadLocalRandom.current().nextInt(adjacentBlockFaces.length)];
        } else {
            direction = surroundingBlockFaces[ThreadLocalRandom.current().nextInt(surroundingBlockFaces.length)];
        }
        consumer.accept(getRelativeBlock(direction, snapshot), direction);
    }

    public List<StringBlock> getAdjacentBlocks(ChunkSnapshot snapshot, boolean includeVertical) {
        return Arrays.stream(includeVertical ? adjacentBlockFaces : surroundingBlockFaces)
                .map(blockFace -> getRelativeBlock(blockFace, snapshot))
                .collect(Collectors.toList());
    }

    public StringBlock get() {
        return new StringBlock(getWorld(), getBlockX(), getBlockY(), getBlockZ(), blockData);
    }

    public void set() {
        set(doBlockUpdate());

        if (onSetRunnable != null) {
            onSetRunnable.run();
        }
    }

    public void set(boolean notify) {
        if (empty) return;

        if (blockStateHolder != null) {
            try {
                adapt(getWorld()).setBlock(asBlockVector(this), blockStateHolder, notify);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } else {
            setBlock(blockData, notify);
        }
    }

    public BlockData getBlockData() {
        if (blockData == null) {
            blockData = getBlock().getBlockData();
        }

        return blockData;
    }

    public void setDoBlockUpdate(boolean doBlockUpdate) {
        this.doBlockUpdate = doBlockUpdate;
    }

    public boolean willBlockUpdate() {
        return doBlockUpdate != null ? doBlockUpdate : false;
    }

    public boolean doBlockUpdate() {
        //If it's not null, it's been explicitly set true/false, so return its value
        if (doBlockUpdate != null) return doBlockUpdate;

        return false;
    }

    public void setDoLightUpdate(boolean doLightUpdate) {
        this.doLightUpdate = doLightUpdate;
    }

    public void update() {
        getBlock().getState().update();
    }

    public boolean isLiquid(boolean checkBlock) {
        Material type = checkBlock ? getBlockType() : getType();
        return type == Material.LAVA || type == Material.WATER;
    }

    public boolean isSlabHalf(Slab.Type half) {
        if (blockData instanceof Slab) {
            return ((Slab) blockData).getType() == half;
        }

        return false;
    }

    public String getFriendlyName() {
        return ItemStackUtil.getFriendlyName(getType());
    }

    @Override
    public Inventory getInventory() {
        try {
            if (getBlock().getState(false) instanceof Container) {
                Container c = (Container) getBlock().getState();
                return c.getInventory();
            }
        } catch (Exception ignored) {
            //Sometimes you can just fail to read a blockstate..?
        }

        return null;
    }

    public boolean isValidBottomStackedBlock(ChunkSnapshot snapshot) {
        Material type = getType();
        StringBlock below = getRelativeBlock(BlockFace.DOWN, snapshot);
        if (type == Material.SUGAR_CANE) {
            return below.getType() != Material.SUGAR_CANE
                    && below.getType().isOccluding()
                    && below.getAdjacentBlocks(snapshot, false).stream().anyMatch(stringBlock -> stringBlock.getType() == Material.WATER);
        } else if (type == Material.CACTUS) {
            return below.getType() == Material.SAND;
        }
        return false;
    }

    public boolean isSameAsWorld() {
        return getBlock().getBlockData().equals(blockData);
    }

    @Override
    public int compareTo(StringBlock o) {
        int d = Double.compare(getX(), o.getX());
        if (d == 0) {
            d = Double.compare(getY(), o.getY());
            if (d == 0) {
                d = Double.compare(getZ(), o.getZ());
            }
        }
        return d;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return LocationUtil.coordsToStringBasic(this);
    }

    public Integer getPriority() {
        return priority;
    }

    public <T extends BlockStateHolder<T>> void setBlockStateHolder(T block) {
        this.blockStateHolder = block;
    }
}
