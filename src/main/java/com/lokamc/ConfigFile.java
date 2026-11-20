package com.lokamc;

import com.lokamc.types.StringBlock;
import com.lokamc.types.StringLocation;
import net.md_5.bungee.api.ChatColor;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.lokamc.utils.SerializeInventory.fromBase64ToItem;
import static java.lang.Float.parseFloat;

@SuppressWarnings("unused")
public class ConfigFile {
    public final Plugin plugin;
    private final String fileName;
    public final File file;
    public FileConfiguration fileConfiguration;
    //Callers make a new ConfigFile per use, so locking and pending saves are shared per file, not per instance
    private static final Map<String, ReentrantLock> FILE_LOCKS = new ConcurrentHashMap<>();
    private static final Map<String, String> PENDING_SAVES = new ConcurrentHashMap<>();
    private final String path;

    public ConfigFile(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
        this.path = file.getAbsolutePath();
    }

    public byte getByte(String key, byte defaultValue) {
        if (get(key, defaultValue) == null) return defaultValue;
        return Byte.parseByte(get(key, defaultValue));
    }

    public <T extends Enum> T getEnum(String key, Class<T> enumClass) {
        return getEnum(key, enumClass, null);
    }

    public <T extends Enum> T getEnum(String key, Class<T> enumClass, T defaultValue) {
        String value = get(key, null);
        if (value != null) {
            try {
                return (T) Enum.valueOf(enumClass, value.toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return defaultValue;
    }

    public int getInt(String key) {
        if (get(key, 0) == null) return 0;
        try {
            return Integer.parseInt(get(key, 0));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getInt(String key, int defaultValue) {
        if (get(key, defaultValue) == null) return defaultValue;
        try {
            return Integer.parseInt(get(key, defaultValue));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int getInt(String key, Integer defaultValue) {
        try {
            return Integer.parseInt(get(key, defaultValue));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int[] getIntRange(String key) {
        String range = get(key, null);
        if (range == null) return null;

        String[] split = range.split("-");
        int[] amountRange = new int[2];
        amountRange[0] = Integer.valueOf(split[0]);
        amountRange[1] = Integer.valueOf(split[1]);
        return amountRange;
    }

    public long getLong(String key) {
        return Long.parseLong(get(key, 0L));
    }

    public long getLong(String key, Long defaultValue) {
        return Long.parseLong(get(key, defaultValue));
    }

    public float getFloat(String key) {
        return parseFloat(get(key, 0f));
    }

    public float getFloat(String key, Float defaultValue) {
        return parseFloat(get(key, defaultValue));
    }

    public double getDouble(String key) {
        return Double.parseDouble(get(key, 0d));
    }

    public double getDouble(String key, Double defaultValue) {
        return Double.parseDouble(get(key, defaultValue));
    }

    public Boolean getBool(String key) {
        return Boolean.parseBoolean(get(key, false));
    }

    public Boolean getBool(String key, Boolean defaultValue) {
        return Boolean.parseBoolean(get(key, defaultValue));
    }

    public StringLocation getStringLoc(String key) {
        String value = get(key, null);
        return value != null ? StringLocation.fromArgs(value) : null;
    }

    public StringBlock getStringBlock(String key) {
        return getStringBlock(key, true);
    }

    public StringBlock getStringBlock(String key, boolean andLoad) {
        String value = get(key, null);
        if (value != null) {
            Location loc = getLoc(key);
            if (andLoad) {
                return new StringBlock(loc.getBlock());
            } else {
                return new StringBlock(loc);
            }
        }

        return null;
    }

    public List<String> getList(String key) {
        return getConfig().getStringList(key);
    }

    public Set<String> getSet(String key) {
        return new HashSet<>(getConfig().getStringList(key));
    }

    public Location getLoc(String key) {
        String loc = get(key, null);
        if (loc == null) return null;

        String[] elems = loc.split(",");
        World world = plugin.getServer().getWorld(elems[0]);
        return new Location(world, Double.parseDouble(elems[1]),
                Double.parseDouble(elems[2]), Double.parseDouble(elems[3]),
                elems.length > 4 ? parseFloat(elems[4]) : 0f,
                elems.length > 5 ? parseFloat(elems[5]) : 0f);
    }

    public Material getMaterial(String key) {
        String material = get(key, "AIR");
        try {
            return Material.valueOf(material.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
            return Material.AIR;
        }
    }

    public Material getMaterial(String key, Material defaultValue) {
        String material = get(key, null);
        if (material == null) {
            return defaultValue;
        }

        try {
            return Material.valueOf(material.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public BlockData getBlockData(String key) {
        String data = get(key);
        if (data == null) return Material.AIR.createBlockData();
        return Bukkit.createBlockData(data);
    }

    public String getColored(String key) {
        return getColored(key, null);
    }

    public String getColored(String key, Object defaultValue) {
        String value = get(key, defaultValue);
        return value != null ? ChatColor.translateAlternateColorCodes('&', value) : null;
    }

    public ItemStack getItem(String key, Material defaultMaterial) {
        ItemStack item = getItem(key);
        return item != null ? item : ItemStack.of(defaultMaterial);
    }

    @Deprecated
    public ItemStack getItem(String key) {
        String item = get(key, null);
        if (item == null) return null;

        try {
            return fromBase64ToItem(item);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ItemStack getItemStack(String key) {
        return getItemStack(key, null);
    }

    public ItemStack getItemStack(String key, ItemStack defaultItemStack) {
        String item = get(key, null);
        if (item == null) return defaultItemStack;

        try {
            return getConfig().getItemStack(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultItemStack;
    }

    public ObjectId getObjectId(String key) {
        String id = get(key, null);
        return id != null ? new ObjectId(id) : null;
    }

    public World getWorld(String worldName) {
        return getWorld(worldName, null);
    }

    public World getWorld(String worldName, World defaultWorld) {
        String world = get(worldName);
        if (world != null) {
            return Bukkit.getWorld(world);
        }

        return defaultWorld;
    }

    public Date getDate(String key, DateFormat simpleDateFormat) {
        String dateString = get(key, null);
        if (dateString == null) return null;

        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public UUID getUUID(String key) {
        String id = get(key, null);
        return id != null ? UUID.fromString(id) : null;
    }

    public AxisAngle4f getAxisAngle4f(String key) {
        return getAxisAngle4f(key, new AxisAngle4f());
    }

    public AxisAngle4f getAxisAngle4f(String key, AxisAngle4f defaultValue) {
        String value = get(key, null);
        if (value == null) return defaultValue;

        try {
            String[] split = value.split(",");
            return new AxisAngle4f(parseFloat(split[0]), parseFloat(split[1]), parseFloat(split[2]), parseFloat(split[3]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public Quaternionf getQuaternionf(String key) {
        return getQuaternionf(key, new Quaternionf());
    }

    public Quaternionf getQuaternionf(String key, Quaternionf defaultValue) {
        String value = get(key, null);
        if (value == null) return defaultValue;

        try {
            String[] split = value.split(",");
            return new Quaternionf(parseFloat(split[0]), parseFloat(split[1]), parseFloat(split[2]), parseFloat(split[3]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, Object defaultValue) {
        String value = getConfig().getString(key);
        if (value != null) {
            return value;
        } else {
            if (defaultValue == null) {
                //Because I want strings back, I can't return a string called null, so manually doing it.
                return null;
            } else {
                //Return the variable default value as string.
                return defaultValue.toString();
            }
        }
    }

    public List<String> getValues(String section) {
        ConfigurationSection configSection = getConfig().getConfigurationSection(section);
        if (configSection == null) return new ArrayList<>();

        Map<String, Object> valueMap = configSection.getValues(false);
        List<String> values = new ArrayList<>(valueMap.size());
        for (Object val : valueMap.values()) {
            values.add(val.toString());
        }
        return values;
    }

    public Set<String> getKeys(String key) {
        ConfigurationSection section = getConfig().getConfigurationSection(key);
        if (section == null) return new HashSet<>();

        return section.getKeys(false);
    }

    public List<String> getAll(String key) {
        ConfigurationSection section = getConfig().getConfigurationSection(key);
        if (section != null) {
            return new ArrayList<>(section.getKeys(false));
        } else {
            return new ArrayList<>();
        }
    }

    public void set(String key, Object value) {
        if (value instanceof AxisAngle4f axisAngle4f) {
            getConfig().set(key, axisAngle4f.angle + "," + axisAngle4f.x + "," + axisAngle4f.y + "," + axisAngle4f.z);
        } else if (value instanceof Quaternionf quaternionf) {
            getConfig().set(key, quaternionf.x + "," + quaternionf.y + "," + quaternionf.z + "," + quaternionf.w);
        } else {
            getConfig().set(key, value);
        }
    }

    public void reloadConfig() {
        //A save that hasn't reached the disk yet is newer than the file, so read that instead
        String pending = PENDING_SAVES.get(path);
        if (pending != null) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(pending);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
            fileConfiguration = config;
        } else {
            ReentrantLock lock = lockFor(path);
            lock.lock();
            try {
                fileConfiguration = YamlConfiguration.loadConfiguration(file);
            } finally {
                lock.unlock();
            }
        }

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            fileConfiguration.setDefaults(defConfig);
        }
    }

    private FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }

    public synchronized void save() {
        save(null);
    }

    public synchronized void saveSync() {
        PENDING_SAVES.put(path, getConfig().saveToString());
        writeLatest(null);
    }

    public synchronized void save(Runnable onComplete) {
        //Serialized now, on the caller's thread, so later set() calls can't change what this save writes
        PENDING_SAVES.put(path, getConfig().saveToString());
        Thread.ofVirtual().start(() -> writeLatest(onComplete));
    }

    /**
     * Writes the newest pending save for this path. The text goes to a temp file that is then renamed over the real
     * one, so a reader on any server sees the whole old file or the whole new one. Writing the file in place
     * truncated it first, and a reader landing in that gap saw an empty config and saved it back (11-20 tourny.yml).
     * Saves can finish out of order, so each writes whatever is newest and a later one finding nothing pending skips.
     */
    private void writeLatest(Runnable onComplete) {
        ReentrantLock lock = lockFor(path);
        Path target = file.toPath();
        Path temp = null;
        lock.lock();
        try {
            String data = PENDING_SAVES.get(path);
            if (data != null) {
                Path dir = target.toAbsolutePath().getParent();
                Files.createDirectories(dir);
                temp = Files.createTempFile(dir, file.getName() + ".", ".tmp");
                Files.writeString(temp, data, StandardCharsets.UTF_8);
                copyPermissions(target, temp);
                moveIntoPlace(temp, target);
                temp = null;
                PENDING_SAVES.remove(path, data);
            }

            if (onComplete != null) {
                onComplete.run();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (Exception ignored) {
                }
            }
            lock.unlock();
        }
    }

    //Windows refuses to replace a file someone has open (local test servers), so retry briefly there
    private static void moveIntoPlace(Path temp, Path target) throws java.io.IOException, InterruptedException {
        for (int attempt = 1; ; attempt++) {
            try {
                try {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            } catch (java.nio.file.AccessDeniedException e) {
                if (attempt >= 50) throw e;

                Thread.sleep(10);
            }
        }
    }

    //Temp files are created owner-only; keep the file readable the way it was (rw-r--r-- for a new one)
    private static void copyPermissions(Path from, Path to) {
        try {
            Set<PosixFilePermission> perms = Files.exists(from)
                    ? Files.getPosixFilePermissions(from)
                    : PosixFilePermissions.fromString("rw-r--r--");
            Files.setPosixFilePermissions(to, perms);
        } catch (UnsupportedOperationException | java.io.IOException ignored) {
            //Not a POSIX filesystem (local Windows test servers)
        }
    }

    private static ReentrantLock lockFor(String path) {
        return FILE_LOCKS.computeIfAbsent(path, p -> new ReentrantLock());
    }

    @Override
    public String toString() {
        return "[Config " + file.getName() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConfigFile configFile && configFile.fileName.equals(fileName);
    }
}