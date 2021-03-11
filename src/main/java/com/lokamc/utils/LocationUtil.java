package com.lokamc.utils;

import com.lokamc.types.StringChunk;
import com.lokamc.types.StringLocation;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static org.bukkit.Material.AIR;

public class LocationUtil {
    public static Location parseCoord(String string) {
        if (string == null) return null;

        String[] elems = string.split(",");
        World world = Bukkit.getWorld(elems[0]);
        return new Location(world, parseDouble(elems[1]),
                parseDouble(elems[2]), parseDouble(elems[3]),
                elems.length > 4 ? parseFloat(elems[4]) : 0f,
                elems.length > 5 ? parseFloat(elems[5]) : 0f);
    }

    public static String coordsToString(com.sk89q.worldedit.math.BlockVector3 point) {
        if (point == null) return null;

        return point.getX() + "," +
                point.getY() + "," +
                point.getZ();
    }

    public static String coordsToString(StringLocation point) {
        if (point == null) return null;

        World w = point.getWorld();
        String worldName = w != null ? w.getName() : point.getWorldName();
        String coords = worldName + "," +
                point.getX() + "," +
                point.getY() + "," +
                point.getZ();

        if (point.getYaw() > 0f || point.getPitch() > 0) {
            coords += "," + point.getYaw();
            coords += "," + point.getPitch();
        }

        return coords;
    }

    public static String coordsToString(Location point) {
        if (point == null) return null;

        String coords = point.getWorld().getName() + "," +
                point.getX() + "," +
                point.getY() + "," +
                point.getZ();

        if (point.getYaw() > 0f || point.getPitch() > 0) {
            coords += "," + point.getYaw();
            coords += "," + point.getPitch();
        }

        return coords;
    }

    public static String coordsToStringBasic(Location point) {
        if (point == null) return null;

        return point.getWorld().getName() + "," +
                point.getBlockX() + "," +
                point.getBlockY() + "," +
                point.getBlockZ();
    }

    public static String prettyCoords(StringLocation l) {
        return (int) l.getX() + ", " + (int) l.getY() + ", " + (int) l.getZ();
    }

    public static String prettyCoords(Location l) {
        return l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ();
    }

    public static String prettyCoords(Location l, boolean showY) {
        if (showY) return l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ();
        return l.getBlockX() + "x, " + l.getBlockZ() + "z";
    }

    public static Location getLocationFromChest(Inventory i) {
        return i.getHolder() instanceof Chest ? ((Chest) i.getHolder()).getLocation() : null;
    }

    public static Inventory getChestInventory(Location chestLocation) {
        return getChestInventory(chestLocation.getBlock());
    }

    public static Inventory getChestInventory(Block block) {
        BlockState state = block.getState();
        if (state instanceof Container) {
            return ((Container) state).getInventory();
        } else {
            return null;
        }
    }

    public static boolean hasWGBuildPerms(Player p, Location l) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.createQuery().testState(BukkitAdapter.adapt(l), localPlayer, Flags.BUILD);
    }

    public static boolean inWGRegion(String region, String player) {
        Player p = Bukkit.getPlayerExact(player);
        return inWGRegion(p, region);
    }

    public static boolean inWGRegion(Player p, String region) {
        return p != null && getWGRegions(p.getLocation()).contains(region);
    }

    public static boolean inWGRegion(Location l, String region) {
        return l != null && getWGRegions(l).contains(region);
    }

    public static boolean inAnyWGRegion(Location l, String... region) {
        return l != null && Arrays.stream(region).anyMatch(getWGRegions(l)::contains);
    }

    public static boolean inAnyWGRegion(Location l, Collection<String> regions) {
        return l != null && regions != null && regions.stream().anyMatch(getWGRegions(l)::contains);
    }

    public static boolean inWGRegion(Entity e, String region) {
        return e != null && getWGRegions(e.getLocation()).contains(region);
    }

    public static Set<String> getWGRegions(World world) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        return regionManager != null ? regionManager.getRegions().keySet() : new HashSet<>();
    }

    public static List<String> getWGRegions(Entity e) {
        return getWGRegions(e.getLocation());
    }

    public static List<String> getWGRegions(Location loc) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
        Iterator<ProtectedRegion> prs = set.iterator();
        List<String> regions = new ArrayList<>(set.size());
        while (prs.hasNext()) {
            ProtectedRegion pr = prs.next();
            regions.add(pr.getId());
        }
        return regions;
    }

    public static Location getRandom(Location loc, double r, int min) {
        // given loc as the centre of the area you want, r as the max radius...
        double a = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        double dist = min + ThreadLocalRandom.current().nextDouble() * r;
        return loc.clone().add(dist * Math.sin(a), 0, dist * Math.cos(a));
    }

    public static ProtectedRegion getWGRegion(World world, String region) {
        RegionManager query = getRegionContainer(world);
        if (query != null) {
            return query.getRegion(region);
        }

        return null;
    }

    public static RegionManager getRegionContainer(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    public static List<Entity> getEntitiesInRegion(World world, String region, int radius) {
        List<Entity> entities = new ArrayList<>();
        Location center = getCenter(world, region);
        if (center == null) return entities;

        for (Entity e : center.getNearbyLivingEntities(radius)) {
            if (e.hasMetadata("NPC")) continue;

            if ((e instanceof Player || e instanceof Animals) && inWGRegion(e, region)) {
                entities.add(e);
            }
        }
        return entities;
    }

    public static List<Player> getPlayersInRegion(World world, String region) {
        List<Player> players = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.hasMetadata("npc")) continue;

            if (inWGRegion(p, region)) {
                players.add(p);
            }
        }
        return players;
    }

    public static boolean isChunkLoaded(World world, int x, int z) {
        return world.isChunkLoaded(x, z);
    }

    public static boolean playerMoved(Location from, Location to) {
        //If these values don't change, this was just a head turn and we don't care.
        return !(from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ());
    }

    public static int getAngle(Player p) {
        float yaw = p.getLocation().getYaw();
        return (((int) ((yaw + 405) / 90)) % 4) * 90;
    }

    public static int getAngle(float yaw) {
        return (((int) ((yaw + 405) / 90)) % 4) * 90;
    }

    public static int getOddFixedAngle(int angle) {
        if (angle == 0) {
            angle = 180;
        } else if (angle == 180) {
            angle = 0;
        }

        return angle;
    }

    public static List<Block> getBlocksFromRegion(Region region) {
        return getBlocksFromRegion(getRegionMinimum(region), getRegionMaximum(region));
    }

    public static List<Block> getBlocksFromRegion(World world, ProtectedRegion region) {
        BlockVector3 minimumPoint = region.getMinimumPoint();
        BlockVector3 maximumPoint = region.getMaximumPoint();
        Location l1 = new Location(world, minimumPoint.getX(), minimumPoint.getY(), minimumPoint.getZ());
        Location l2 = new Location(world, maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());
        return getBlocksFromRegion(l1, l2);
    }

    public static List<Block> getBlocksFromRegion(Block b1, Block b2) {
        return getBlocksFromRegion(b1.getLocation(), b2.getLocation(), 0);
    }

    public static List<Block> getBlocksFromRegion(Location l, int outset) {
        Location p1 = l.clone().add(outset, outset, outset);
        Location p2 = l.clone().subtract(outset, outset, outset);
        return getBlocksFromRegion(p1, p2, 0);

    }

    public static List<Block> getBlocksFromRegion(Location p1, Location p2) {
        return getBlocksFromRegion(p1, p2, 0);
    }

    private static List<Block> getBlocksFromRegion(Location p1, Location p2, int outset) {
        List<Block> blocks = new ArrayList<>();
        int minX = Math.min(p1.getBlockX(), p2.getBlockX()) - outset;
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX()) + outset;
        int minY = Math.min(p1.getBlockY(), p2.getBlockY()) - outset;
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY()) + outset;
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ()) - outset;
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ()) + outset;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(p1.getWorld().getBlockAt(x, y, z));
                }
            }
        }

        return blocks;
    }

    public static List<StringChunk> getStringChunksFromRegion(Location p1, Location p2) {
        int minX = Math.min(p1.getBlockX(), p2.getBlockX()) >> 4;
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX()) >> 4;
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ()) >> 4;
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ()) >> 4;

        List<StringChunk> chunks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(new StringChunk(p1.getWorld(), x, z));
            }
        }

        return chunks;
    }

    public static List<Chunk> getChunksFromRegion(Location p1, Location p2) {
        int minX = Math.min(p1.getBlockX(), p2.getBlockX()) >> 4;
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX()) >> 4;
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ()) >> 4;
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ()) >> 4;

        List<Chunk> chunks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(p1.getWorld().getChunkAt(x, z));
            }
        }

        return chunks;
    }

    public static List<Block> getRandomBlockRadius(Location source, int randX, int randY, int randZ) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return getBlocksFromRegion(source.clone().add(r.nextInt(randX) + 2, r.nextInt(randY) + 2, r.nextInt(randZ) + 2),
                source.clone().subtract(r.nextInt(randX) + 2, r.nextInt(randY) + 2, r.nextInt(randZ) + 2));
    }

    public static Block getTargetBlock(Player player, int range) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();

        Block b = null;

        for (int i = 0; i <= range; i++) {
            b = loc.add(dir).getBlock();
            if (b.getType() != AIR) return b;
        }

        return b;
    }

    public static Location getCenter(World world, String region) {
        ProtectedRegion pr = getWGRegion(world, region);
        return pr != null ? getCenter(world, pr) : null;
    }

    public static Location getCenter(World world, ProtectedRegion region) {
        Location l1 = new Location(world, region.getMaximumPoint().getBlockX(),
                region.getMaximumPoint().getBlockY(),
                region.getMaximumPoint().getBlockZ());
        Location l2 = new Location(world, region.getMinimumPoint().getBlockX(),
                region.getMinimumPoint().getBlockY(),
                region.getMinimumPoint().getBlockZ());
        return getCenter(l1, l2);
    }

    public static Location[] getRegionEdges(World world, ProtectedRegion region) {
        Location l1 = new Location(world, region.getMaximumPoint().getBlockX(),
                region.getMaximumPoint().getBlockY(),
                region.getMaximumPoint().getBlockZ());
        Location l2 = new Location(world, region.getMinimumPoint().getBlockX(),
                region.getMinimumPoint().getBlockY(),
                region.getMinimumPoint().getBlockZ());
        return new Location[]{l1, l2};
    }

    private static Location getCenter(Location l1, Location l2) {
        return l1.toVector().getMidpoint(l2.toVector()).toLocation(l1.getWorld());
    }

    public static Block standingOn(Entity e) {
        return e.getLocation().getBlock().getRelative(BlockFace.DOWN);
    }

    public static Block standingOn(Entity e, boolean includeJumping) {
        if (includeJumping) {
            return getGround(e.getLocation().getBlock(), 2);
        } else {
            return standingOn(e);
        }
    }

    public static Block standingOnHalfBlock(Entity e) {
        return e.getLocation().getBlock();
    }

    public static com.sk89q.worldedit.math.BlockVector3 toVector(Location l) {
        return BukkitAdapter.asBlockVector(l);
    }

    public static com.sk89q.worldedit.math.BlockVector3 toVector(int x, int y, int z) {
        return Vector3.toBlockPoint(x, y, z);
    }

    public static Location toLocation(World world, com.sk89q.worldedit.math.BlockVector3 vector) {
        return new Location(world, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static Location toLocation(World world, com.sk89q.worldedit.math.Vector3 vector) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public static Location toLocation(World world, Vector vector) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public static Location getRegionMinimum(Region region) {
        return toLocation(BukkitAdapter.asBukkitWorld(region.getWorld()).getWorld(), region.getMinimumPoint());
    }

    public static Location getRegionMaximum(Region region) {
        return toLocation(BukkitAdapter.asBukkitWorld(region.getWorld()).getWorld(), region.getMaximumPoint());
    }

    public static boolean isAdjacentTo(Block sourceBlock, Location l, boolean searchUpDown) {
        for (Block b : getAdjacentBlocks(sourceBlock, searchUpDown)) {
            if (b.getLocation().equals(l)) return true;
        }

        return false;
    }

    public static boolean isAdjacentTo(Block sourceBlock, Material m, boolean searchUpDown) {
        for (Block b : getAdjacentBlocks(sourceBlock, searchUpDown)) {
            if (b.getType() == m) return true;
        }

        return false;
    }

    public static List<Block> getAdjacentBlocks(Block b, boolean searchUpDown) {
        List<Block> blocks = new ArrayList<>(Arrays.asList(b,
                b.getRelative(BlockFace.EAST),
                b.getRelative(BlockFace.NORTH),
                b.getRelative(BlockFace.SOUTH),
                b.getRelative(BlockFace.WEST)));
        if (searchUpDown) {
            blocks.add(b.getRelative(BlockFace.UP));
            blocks.add(b.getRelative(BlockFace.DOWN));
        }
        return blocks;
    }

    public static Block getAdjacentBlock(Block b, int angle) {
        switch (angle) {
            case 0:
                return b.getRelative(BlockFace.NORTH);
            case 90:
                return b.getRelative(BlockFace.EAST);
            case 180:
                return b.getRelative(BlockFace.SOUTH);
            case 270:
                return b.getRelative(BlockFace.WEST);
        }
        return b;
    }

    public static List<Block> getOutset(Location l, int radius, boolean wallsOnly) {
        return getOutset(l, radius, wallsOnly, false);
    }

    public static List<Block> getOutset(Location l, int radius, boolean wallsOnly, boolean hollow) {
        List<Block> blocks = new ArrayList<>();

        Location l1 = l.clone().add(radius, radius, radius);
        Location l2 = l.clone().subtract(radius, radius, radius);

        int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
        int maxY = Math.max(l1.getBlockY(), l2.getBlockY());
        int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());

        int minX = Math.min(l1.getBlockX(), l2.getBlockX());
        int minY = Math.min(l1.getBlockY(), l2.getBlockY());
        int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());

        if (wallsOnly) {
            for (Block b : getBlocksFromRegion(l1, l2)) {
                if (b.getX() == maxX || b.getX() == minX
                        || (!hollow && (b.getY() == maxY || b.getY() == minY))
                        || b.getZ() == maxZ || b.getZ() == minZ) blocks.add(b);
            }
        } else {
            blocks.addAll(getBlocksFromRegion(l1, l2));
        }
        return blocks;
    }

    public static float[] getMapCoords(Location l) {
        float latCoord;
        float longCoord;
        switch (l.getWorld().getName()) {
            case "south":
                latCoord = l.getBlockZ() * 0.00009065332f;
                longCoord = l.getBlockX() * 0.00008864033f;
                return new float[]{latCoord, longCoord};
            case "west":
                latCoord = l.getBlockZ() * 0.000085f;
                longCoord = l.getBlockX() * 0.00011f;
                return new float[]{latCoord, longCoord};
            case "north":
                latCoord = l.getBlockZ() * 0.000087f;
                longCoord = l.getBlockX() * 0.000087f;
                return new float[]{latCoord, longCoord};
        }
        return new float[]{0, 0};
    }

    public static Location getFirstSolidBlockDown(Location l) {
        return getFirstSolidBlockDown(l, -1);
    }

    public static Location getFirstSolidBlockDown(Location l, int maxDistance) {
        Block below = l.getBlock();
        int distance = 1;
        while (!below.getType().isSolid()) {
            below = below.getRelative(BlockFace.DOWN);

            if (maxDistance > 0 && distance++ >= maxDistance) break;
            if (below.getY() < 5) break;
        }
        return below.getLocation();
    }

    public static Block getSurfaceBlockAbove(Block b, int max) {
        Block current = b;

        int count = 0;
        while (!current.getType().isTransparent()) {
            Block above = current.getRelative(BlockFace.UP);
            if (above.getType().isTransparent() || ++count >= max) break;
            current = above;
        }
        return current;
    }

    public static Block getGround(Block b, int max) {
        Block current = b;

        int count = 0;
        while (!current.getType().isSolid()) {
            Block below = current.getRelative(BlockFace.DOWN);
            if (below.getType().isSolid()) return below;

            if (++count >= max) break;
            current = below;
        }
        return current;
    }

    public static boolean isEntityWithinLOS(Player p, Vector startPos,
                                            float degrees, Vector direction) {
        Vector relativePosition = p.getLocation().toVector(); // Position of the entity relative to the cone origin
        relativePosition.subtract(startPos);

        return getAngleBetweenVectors(direction, relativePosition) > degrees;
    }

    public static float getAngleBetweenVectors(Vector v1, Vector v2) {
        return Math.abs((float) Math.toDegrees(v1.angle(v2)));
    }

    public static BlockFace getFaceFromAngle(int angle) {
        switch (angle) {
            case 0:
                return BlockFace.NORTH;
            case 90:
                return BlockFace.EAST;
            case 180:
                return BlockFace.SOUTH;
            case 270:
                return BlockFace.WEST;
        }
        return BlockFace.NORTH;
    }

    // https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection
    // this could be optimised.
    public static boolean intersect(Vector direction, Vector orig, Vector max, Vector min) {
        double tmin = (min.getX() - orig.getX()) / direction.getX();
        double tmax = (max.getX() - orig.getX()) / direction.getX();

        if (tmin > tmax) {
            double t = tmax;
            tmax = tmin;
            tmin = t;
        }

        double tymin = (min.getY() - orig.getY()) / direction.getY();
        double tymax = (max.getY() - orig.getY()) / direction.getY();

        if (tymin > tymax) {
            double t = tymin;
            tymin = tymax;
            tymax = t;
        }

        if ((tmin > tymax) || (tymin > tmax))
            return false;

        if (tymin > tmin)
            tmin = tymin;

        if (tymax < tmax)
            tmax = tymax;

        double tzmin = (min.getZ() - orig.getZ()) / direction.getZ();
        double tzmax = (max.getZ() - orig.getZ()) / direction.getZ();

        if (tzmin > tzmax) {
            double t = tzmax;
            tzmax = tzmin;
            tzmin = t;
        }

        return !((tmin > tzmax) || (tzmin > tmax));
    }

    public static Block getLastBlockInDirection(Block b, Material m, BlockFace face) {
        Block block = b;
        while (block.getRelative(face).getType() == m) {
            block = block.getRelative(face);
        }
        return block;
    }

    public static Location getSphereClosestPoint(Location c, double radius, Location p) {
        com.sk89q.worldedit.math.BlockVector3 v = toVector(p).subtract(toVector(c)); // v.x = p.x - c.x, v.y = p.y - c.y, v.z = p.z - c.z
        double length = v.length(); // = sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        double x = c.getX() + (radius * ((p.getX() - c.getX()) / length));
        double y = c.getY() + (radius * ((p.getY() - c.getY()) / length));
        double z = c.getZ() + (radius * ((p.getZ() - c.getZ()) / length));
        return new Location(c.getWorld(), x, y, z);
    }

    public static Location getFacingLocation(Location l, Location target, boolean includePitch) {
        Vector dir = target.clone().subtract(l).toVector();
        l.setDirection(dir);

        if (!includePitch) {
            l.setPitch(0);
        }
        return l;
    }

    public static CuboidRegion getExpandedRegion(CuboidRegion region, int expandAmount) {
        com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint().subtract(expandAmount, expandAmount, expandAmount);
        com.sk89q.worldedit.math.BlockVector3 max = region.getMaximumPoint().add(expandAmount, expandAmount, expandAmount);
        return new CuboidRegion(min, max);
    }

    public static String getRegionFile(Location l) {
        return getRegionFile(l.getChunk().getX(), l.getChunk().getZ());
    }

    /**
     * Get the filename of a region file.
     *
     * @return the filename
     */
    public static String getRegionFile(int x, int z) {
        return "r." + (x >> 5) + "." + (z >> 5);
    }
}