package com.lokamc.types;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StringChunk {
    private World world;
    public int x, z;
    private boolean biome;
    private boolean hasTicket;

    public StringChunk(Chunk c) {
        this.world = c.getWorld();
        this.x = c.getX();
        this.z = c.getZ();
    }

    public StringChunk(String args) {
        String[] argsSplit = args.split(",");
        this.world = Bukkit.getWorld(argsSplit[0]);
        this.x = Integer.parseInt(argsSplit[1]);
        this.z = Integer.parseInt(argsSplit[2]);
    }

    public StringChunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }

    public CompletableFuture<Chunk> getChunkAsync() {
        return world.getChunkAtAsync(x, z);
    }

    public boolean useBiome() {
        return biome;
    }

    public void setBiome(boolean biome) {
        this.biome = biome;
    }

    public boolean hasTicket() {
        return hasTicket;
    }

    public void setHasTicket(boolean hasTicket) {
        this.hasTicket = hasTicket;
    }

    public Location getCenter() {
        return new Location(world, x << 4, 64, z << 4)
                .add(7, 0, 7);
    }

    public static Location getCenter(Chunk c) {
        return new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4).add(7, 0, 7);
    }

    public boolean isLoaded() {
        return world.isChunkLoaded(x, z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, getChunkKey());
    }

    public long getChunkKey() {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Chunk) {
            Chunk o = (Chunk) other;
            return x == o.getX() && z == o.getZ() && world.equals(o.getWorld());
        } else if (other instanceof StringChunk) {
            StringChunk o = (StringChunk) other;
            return x == o.x && z == o.z && world.equals(o.world);
        }

        return false;
    }

    @Override
    public String toString() {
        return world.getName() + "," + x + "," + z;
    }
}
