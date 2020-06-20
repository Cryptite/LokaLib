package com.lokamc.types;

import com.lokamc.utils.FutureUtils;
import com.lokamc.utils.LocationUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StringLocation extends Location {
    public StringLocation(Block b) {
        super(b.getWorld(), b.getX(), b.getY(), b.getZ());
    }

    public StringLocation(World w, double x, double y, double z) {
        super(w, x, y, z);
    }

    public StringLocation(World w, double x, double y, double z, float yaw, float pitch) {
        super(w, x, y, z, yaw, pitch);
    }

    public StringLocation(Location l) {
        super(l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public StringLocation(StringLocation l) {
        super(l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public static StringLocation fromArgs(String string) {
        String[] elems = string.split(",");
        World world = Bukkit.getWorld(elems[0]);
        double x = Double.parseDouble(elems[1]);
        double y = Double.parseDouble(elems[2]);
        double z = Double.parseDouble(elems[3]);
        float yaw = elems.length > 4 ? Float.parseFloat(elems[4]) : 0f;
        float pitch = elems.length > 5 ? Float.parseFloat(elems[5]) : 0f;
        return new StringLocation(world, x, y, z, yaw, pitch);
    }

    public StringLocation(World world, BlockVector3 vector3) {
        super(world, vector3.getX(), vector3.getY(), vector3.getZ());
    }

    public boolean inChunk(Chunk c) {
        return c.getWorld().equals(getWorld())
                && c.getX() == getBlockX() >> 4
                && c.getZ() == getBlockZ() >> 4;
    }

    public StringChunk getStringChunk() {
        return getStringChunk(false);
    }

    public StringChunk getStringChunk(boolean clean) {
        World world = getWorld();
        return new StringChunk(clean ? Bukkit.getWorld(world.getName() + "_clean") : world, getBlockX() >> 4, getBlockZ() >> 4);
    }

    public void getBlockAsync(Plugin plugin, Consumer<Block> consumer) {
        FutureUtils.thenRunSync(plugin, getWorld().getChunkAtAsync(this, false), () -> consumer.accept(getBlock()));
    }

    public Chunk getChunk() {
        return getWorld().getChunkAt(this);
    }

    /**
     * @return Returns whether chunk is loaded without actually loading it
     */
    public boolean isChunkLoaded() {
        return getWorld().isChunkLoaded(getBlockX() >> 4, getBlockZ() >> 4);
    }

    public boolean looseEquals(Location l) {
        return getWorld().equals(l.getWorld())
                && getBlockX() == l.getBlockX()
                && getBlockY() == l.getBlockY()
                && getBlockZ() == l.getBlockZ();
    }

    public StringLocation getRelative(BlockFace face) {
        return getRelative(face, 1);
    }

    public StringLocation getRelative(BlockFace face, int amount) {
        int xOffset = face.getModX() * amount;
        int yOffset = face.getModY() * amount;
        int zOffset = face.getModZ() * amount;
        return new StringLocation(getWorld(), getBlockX() + xOffset, getBlockY() + yOffset, getBlockZ() + zOffset);
    }

    public Location getCenter() {
        return getBlock().getLocation().add(.5, .5, .5);
    }

    public Location getBottomCenter() {
        return getBlock().getLocation().add(.5, 0, .5);
    }

    public Location getTopCenter() {
        return getBlock().getLocation().add(.5, 1, .5);
    }

    public BlockVector3 toBlockVector() {
        return Vector3.toBlockPoint(getBlockX(), getBlockY(), getBlockZ());
    }

    public Inventory getInventory() {
        return LocationUtil.getChestInventory(this);
    }

    public void setLocation(Location l) {
        setWorld(l.getWorld());
        setX(l.getX());
        setY(l.getY());
        setZ(l.getZ());
        setYaw(l.getYaw());
        setPitch(l.getPitch());
    }

    @Override
    public String toString() {
        return LocationUtil.coordsToString(this);
    }

    public String toStringBasic() {
        return LocationUtil.coordsToStringBasic(this);
    }

    public String getPrettyCoords() {
        return LocationUtil.prettyCoords(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Location) {
            return compareLocation((Location) obj);
        } else if (obj instanceof Block) {
            return compareLocation(((Block) obj).getLocation());
        } else {
            return super.equals(obj);
        }
    }

    private boolean compareLocation(Location other) {
        if (this.getWorld() != other.getWorld() && (this.getWorld() == null || !this.getWorld().equals(other.getWorld()))) {
            return false;
        } else if (Double.doubleToLongBits(this.getX()) != Double.doubleToLongBits(other.getX())) {
            return false;
        } else if (Double.doubleToLongBits(this.getY()) != Double.doubleToLongBits(other.getY())) {
            return false;
        } else if (Double.doubleToLongBits(this.getZ()) != Double.doubleToLongBits(other.getZ())) {
            return false;
        } else if (Float.floatToIntBits(this.getPitch()) != Float.floatToIntBits(other.getPitch())) {
            return false;
        } else {
            return Float.floatToIntBits(this.getYaw()) == Float.floatToIntBits(other.getYaw());
        }
    }

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap result = new LinkedHashMap();
        result.put("coord", toStringBasic());
        return result;
    }
}
