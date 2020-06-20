package com.lokamc;

import com.lokamc.types.StringBlock;
import com.lokamc.types.StringLocation;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import static com.lokamc.utils.SerializeInventory.fromBase64ToItem;

public class ConfigFile {
    public final Plugin plugin;
    private final String fileName;
    public final File file;
    public FileConfiguration fileConfiguration;

    public ConfigFile(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
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
        return Float.parseFloat(get(key, 0f));
    }

    public float getFloat(String key, Float defaultValue) {
        return Float.parseFloat(get(key, defaultValue));
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
                elems.length > 4 ? Float.parseFloat(elems[4]) : 0f,
                elems.length > 5 ? Float.parseFloat(elems[5]) : 0f);
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
        return item != null ? item : new ItemStack(defaultMaterial);
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
        String item = get(key, null);
        if (item == null) return null;

        try {
            return getConfig().getItemStack(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public World getWorld(String worldName) {
        String world = get(worldName);
        if (world != null) {
            return Bukkit.getWorld(world);
        }

        return null;
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
        getConfig().set(key, value);
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

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
        doSave(file, getConfig(), null);
    }

    public synchronized void save(Runnable onComplete) {
        final FileConfiguration config = fileConfiguration;
        LokaLib.configFileExecutor.execute(() -> doSave(file, config, onComplete));
    }

    private void doSave(File file, FileConfiguration config, Runnable onComplete) {
        synchronized (this.file) {
            try {
                config.save(file);

                if (onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
        return obj instanceof ConfigFile && ((ConfigFile) obj).fileName.equals(fileName);
    }
}