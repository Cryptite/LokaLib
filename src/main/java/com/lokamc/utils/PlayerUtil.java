package com.lokamc.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.world.entity.player.Player.MAX_HEALTH;
import static net.minecraft.world.food.FoodConstants.MAX_FOOD;
import static net.minecraft.world.food.FoodConstants.MAX_SATURATION;

public class PlayerUtil {
    public static boolean inValidGameMode(Player p) {
        return p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE;
    }

    public static boolean hasPotionEffect(LivingEntity e, PotionEffectType type) {
        for (PotionEffect potion : e.getActivePotionEffects()) {
            if (potion.getType().equals(type)) return true;
        }

        return false;
    }

    public static boolean hasPotionEffect(LivingEntity e, PotionEffectType type, int level) {
        for (PotionEffect potion : e.getActivePotionEffects()) {
            if (potion.getType().equals(type) && potion.getAmplifier() == level) return true;
        }

        return false;
    }

    public static boolean hasAtLeastPotionEffect(LivingEntity e, PotionEffectType type, int level) {
        for (PotionEffect potion : e.getActivePotionEffects()) {
            if (potion.getType().equals(type) && potion.getAmplifier() >= level) return true;
        }

        return false;
    }

    public static boolean hasGreaterPotionEffect(LivingEntity e, PotionEffect effect) {
        for (PotionEffect potion : e.getActivePotionEffects()) {
            //If the current effect is strong or has more of a duration left, don't do it.
            if (potion.getType().equals(effect.getType())
                    && (potion.getAmplifier() > effect.getAmplifier() || potion.getDuration() > effect.getDuration()))
                return true;
        }

        return false;
    }


    public static boolean hasGreaterPotionEffect(PotionEffect old, PotionEffect effect) {
        //If the current effect is strong or has more of a duration left, don't do it.
        return old.getType().equals(effect.getType())
                && (old.getAmplifier() > effect.getAmplifier() || old.getDuration() > effect.getDuration());
    }

    private static boolean hasGreaterPotionEffect(LivingEntity e, PotionEffectType type, int level, int seconds) {
        for (PotionEffect potion : e.getActivePotionEffects()) {
            //If the current effect is strong or has more of a duration left, don't do it.
            if (potion.getType().equals(type) && (potion.getAmplifier() > level || potion.getDuration() > seconds * 20))
                return true;
        }

        return false;
    }

    /**
     * Gives a max-length potion effect
     */
    public static PotionEffect givePotionEffect(LivingEntity e, PotionEffectType type, int level) {
        return givePotionEffect(e, type, Integer.MAX_VALUE / 20, level);
    }

    /**
     * @param e
     * @param type
     * @param seconds
     * @param level   0 is a level I potion effect.
     */
    public static PotionEffect givePotionEffect(LivingEntity e, PotionEffectType type, int seconds, int level) {
        if (e == null || type == null) return null;

        PotionEffect effect = new PotionEffect(type, 20 * seconds, level);
        e.addPotionEffect(effect);
        return effect;
    }

    public static Boolean hasPotionSpeed(LivingEntity e) {
        //If they have speed with a duration less than 120, it's from a from roads, not speed pots
        for (PotionEffect pot : e.getActivePotionEffects()) {
            if (pot.getType().equals(PotionEffectType.SPEED) && pot.getDuration() >= 121) return true;
        }

        return false;
    }

    public static void removePotionEffects(LivingEntity e) {
        //Remove potion effects
        for (PotionEffect effect : e.getActivePotionEffects()) {
            e.removePotionEffect(effect.getType());
        }
    }

    public static void removePotionEffects(LivingEntity e, PotionEffectType... potionEffectTypes) {
        //Remove potion effects
        for (PotionEffectType type : potionEffectTypes) {
            e.removePotionEffect(type);
        }
    }

    public static Boolean hasBeaconHaste(Player p) {
        //If they have speed with a duration less than 120, it's from a from roads, not speed pots
        for (PotionEffect pot : p.getActivePotionEffects()) {
            if (pot.getType().equals(PotionEffectType.HASTE) && pot.getDuration() <= 160) return true;
        }

        return false;
    }

    public static void loadPlayer(Plugin plugin, UUID uuid, Consumer<Player> consumer) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline()) {
            consumer.accept(p);
        } else {
            CompletableFuture.runAsync(() -> consumer.accept(loadOfflinePlayer(uuid)), Bukkit.getScheduler().getMainThreadExecutor(plugin));
        }
    }

    public static Player loadOfflinePlayer(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerPlayer entity = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        CompoundTag load = server.getPlayerList().playerIo.load(entity).orElse(null);
        CraftPlayer craftPlayer = entity.getBukkitEntity();
        entity.load(load);
        return craftPlayer;
    }

    public static boolean hasEmptyInventory(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null)
                return false;
        }
        return true;
    }

    public static int getEmptyInventorySlots(Player p) {
        return (int) Arrays.stream(p.getInventory().getContents())
                .filter(item -> item == null || item.getType() == Material.AIR)
                .count();
    }

    public static boolean hasItemInHand(Player p, ItemStack item) {
        if (p != null) {
            ItemStack mh = p.getInventory().getItemInMainHand();
            ItemStack oh = p.getInventory().getItemInOffHand();
            if ((mh != null && mh.equals(item) || (oh != null && oh.equals(item)))) {
                return true;
            }
        }
        return false;
    }

    public static boolean inRightHand(Player p, Material m) {
        if (p != null) {
            ItemStack mh = p.getInventory().getItemInMainHand();
            if (mh != null && mh.getType() == m) {
                return true;
            }
        }
        return false;
    }

    public static boolean inRightHand(Player p, ItemStack item) {
        if (p != null) {
            ItemStack mh = p.getInventory().getItemInMainHand();
            if (mh != null && mh.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public record HoldingResult(ItemStack itemStack, EquipmentSlot slot) {
    }

    public static @Nullable HoldingResult getHolding(Player p, Material... materials) {
        if (p == null) return null;

        for (Material m : materials) {
            HoldingResult result = getHolding(p, m);
            if (result != null) return result;
        }

        return null;
    }

    public static @Nullable HoldingResult getHolding(Player p, Material m) {
        if (p == null) return null;

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() == m) return new HoldingResult(item, EquipmentSlot.HAND);

        item = p.getInventory().getItemInOffHand();
        if (item.getType() == m) return new HoldingResult(item, EquipmentSlot.OFF_HAND);

        return null;
    }

    public static void setItemInHandFromItem(Player p, ItemStack holding, ItemStack newItem) {
        if (inRightHand(p, holding))
            p.getInventory().setItemInMainHand(newItem);
        else p.getInventory().setItemInOffHand(newItem);
    }

    public static boolean hasFullInventory(Player p) {
        return p.getInventory().firstEmpty() == -1;
    }

    public static boolean isHolding(Player p, Set<Material> items) {
        for (Material item : items) {
            if (isHolding(p, item)) return true;
        }

        return false;
    }

    public static boolean isHolding(Player p, ItemStack item) {
        return isHolding(p, item, true);
    }

    public static boolean isHolding(Player p, ItemStack item, boolean ignoreCount) {
        ItemStack inHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();

        if (ignoreCount) {
            inHand = inHand.asOne();
            offHand = offHand.asOne();
        }

        return (inHand != null && inHand.equals(item))
                || offHand != null && offHand.equals(item);
    }

    public static boolean isHolding(Player p, Material m) {
        ItemStack inHand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();
        return inHand.getType() == m || offHand.getType() == m;
    }

    public static boolean isHolding(Player p, EquipmentSlot hand, Material m) {
        if (hand.equals(EquipmentSlot.HAND)) {
            return p.getInventory().getItemInMainHand().getType() == m;
        } else if (hand.equals(EquipmentSlot.OFF_HAND)) {
            return p.getInventory().getItemInOffHand().getType() == m;
        }

        return false;
    }

    public static ItemStack findLoredSword(Player p) {
        for (ItemStack item : p.getInventory()) {
            if (ItemStackUtil.hasNBTData(item, "lore")) return item;
        }

        return null;
    }

    public static Block getFirstLOSBlock(Player player) {
        return getFirstLOSBlock(player, 10);
    }

    public static Block getFirstLOSBlock(Player player, int maxDistance) {
        // get the blocks in the players line of sight.
        Iterator<Block> itr = new BlockIterator(player, maxDistance);

        while (itr.hasNext()) {
            Block block = itr.next();

            if (!MaterialSets.bypassLOSBlocks.contains(block.getType())) {
                RayTraceResult result = BoundingBox.of(block)
                        .rayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(), maxDistance);
                return result.getHitBlock();
            }
        }
        return null;
    }

    public static boolean hasLOS(Location source, Location target, int maxDistance) {
        return hasLOS(source, target, null, maxDistance);
    }

    public static boolean hasLOS(Location source, Location target, Collection<Location> excludeBlocks, int maxDistance) {
        // get the blocks in the players line of sight.
        Vector startVector = source.toVector();
        Iterator<Block> itr = new BlockIterator(source.getWorld(),
                startVector,
                target.toVector().subtract(startVector),
                0,
                maxDistance);

        while (itr.hasNext()) {
            Block block = itr.next();

            //If this block is THE block the target is in, then they have LOS, so return no blocking block
            if (target.getBlock().equals(block)) return true;

            if (!MaterialSets.bypassLOSBlocks.contains(block.getType())
                    && (excludeBlocks == null || !excludeBlocks.contains(block.getLocation()))) {
//                Effects.effect(block.getLocation().add(.5f, .5f, .5f), Particle.SMOKE_NORMAL, 2, .05f, 5);
                return false;
            }
        }
        return true;
    }

    public static boolean isWearingGear(Player p) {
        PlayerInventory inv = p.getInventory();
        if (inv.getHelmet() != null) return true;
        if (inv.getBoots() != null) return true;
        if (inv.getChestplate() != null) return true;
        if (inv.getLeggings() != null) return true;

        return false;
    }

    public static String getSkinUrl(PlayerProfile profile) {
        if (profile == null) return null;

        Set<ProfileProperty> properties = profile.getProperties();
        return properties.stream()
                .filter(profileProperty -> profileProperty.getName().equals("textures"))
                .map(ProfileProperty::getValue)
                .findFirst()
                .orElse(null);
    }

    public static ItemStack getHeadByUrl(String url) {
        if (url == null) return new ItemStack(Material.BARRIER);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        if (url.isEmpty()) return head;

        head.setItemMeta(getSkullMeta(url));
        return head;
    }

    public static SkullMeta getSkullMeta(String url) {
        if (url == null || url.isEmpty()) return null;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", url));
        headMeta.setPlayerProfile(profile);
        head.setItemMeta(headMeta);
        return headMeta;
    }

    public static ItemStack getHeadByProfile(GameProfile profile) {
        if (profile == null) return new ItemStack(Material.BARRIER);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public static GameProfile getGameProfile(String url) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString());
        profile.getProperties().put("textures", new Property("textures", url));
        return profile;
    }

    public static boolean isLookingAt(LivingEntity viewer, Entity entity) {
        return isLookingAt(viewer, entity, 0.5f);
    }

    public static boolean isLookingAt(LivingEntity viewer, Entity entity, float angle) {
        Location eyeLocation = viewer.getEyeLocation();
        Vector towardsEntity = entity.getLocation().subtract(eyeLocation).toVector().normalize();
        return eyeLocation.getDirection().distance(towardsEntity) <= angle;
    }

    public static void fullClearPlayerInventory(Player p) {
        p.getInventory().clear();
        Inventory topInventory = p.getOpenInventory().getTopInventory();
        InventoryType type = topInventory.getType();
        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING) {
            topInventory.clear();
        }
        p.setItemOnCursor(null);
    }

    public static void regenPlayer(Player p) {
        p.setHealth(MAX_HEALTH);
        p.setFoodLevel(MAX_FOOD);
        p.setSaturation(MAX_SATURATION);
        p.setExhaustion(0F);
        p.setFireTicks(0);
    }
}
