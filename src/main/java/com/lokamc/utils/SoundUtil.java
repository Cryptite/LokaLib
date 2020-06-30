package com.lokamc.utils;

import com.lokamc.LokaLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

import static com.lokamc.utils.MathUtil.normalize;

public class SoundUtil {
    public static void playCustomSound(Player p, Location l, String sound, SoundCategory category, float volume) {
        //Also play for the player
        p.playSound(l, sound, category, volume, 1);
    }

    public static void playCustomSound(Player p, String sound, SoundCategory category) {
        playCustomSound(p, sound, category, 1f);
    }

    public static void playCustomSound(Player p, String sound, float volume, SoundCategory category) {
        if (p == null) return;

        //Also play for the player
        p.playSound(p.getLocation(), sound.toLowerCase(), category, volume, 1);
    }

    public static void playCustomSound(Player p, String sound, SoundCategory category, float volume) {
        if (p == null) return;

        //Also play for the player
        p.playSound(p.getLocation(), sound.toLowerCase(), category, volume, 1);
    }

    public static void playWorldCustomSound(Location l, String sound, SoundCategory category, float volume) {
        playWorldCustomSound(l, sound, category, 15, volume);
    }

    public static void playWorldCustomSound(Entity e, String sound, SoundCategory category) {
        playWorldCustomSound(e.getLocation(), sound, category, 15, 1);
    }

    public static void playWorldCustomSound(Entity e, String sound, SoundCategory category, float volume) {
        playWorldCustomSound(e.getLocation(), sound, category, 15, volume);
    }

    public static void playWorldCustomSound(Location l, String sound, SoundCategory category) {
        playWorldCustomSound(l, sound, category, 15, 1);
    }

    public static void playWorldCustomSound(Location l, String sound, SoundCategory category, int radius, float volume) {
        if (Bukkit.isPrimaryThread()) {
            playWorldSound(l, sound, category, radius, volume);
        } else {
            Bukkit.getScheduler().runTask(LokaLib.getInstance(), () -> playWorldSound(l, sound, category, radius, volume));
        }
    }

    private static void playWorldSound(Location l, String sound, SoundCategory category, int radius, float volume) {
        //Get all nearby entities within radius blocks
        for (Player p : l.getNearbyPlayers(radius)) {
            p.playSound(l, sound.toLowerCase(), category, volume, 1);
        }
    }

    public static void playWorldCustomSound(Collection<Player> players, Location l, String sound, SoundCategory category, float volume) {
        //Get all nearby entities within radius blocks
        for (Player p : players) {
            p.playSound(l, sound.toLowerCase(), category, volume, 1);
        }
    }

    public static void playDistantExplosion(Location l) {
        for (Player p : l.getWorld().getPlayers()) {
            double distance = p.getLocation().distance(l);
            float vol = 3 - normalize((float) distance, 400, 1200, 1f, 2f);
            playCustomSound(p, "distantexplosion", vol, SoundCategory.AMBIENT);
        }
    }

    public static void playDistantSound(Location l, String sound) {
        for (Player p : l.getWorld().getPlayers()) {
            double distance = p.getLocation().distance(l);
            float vol = 3 - normalize((float) distance, 400, 1200, 1f, 2f);
            playCustomSound(p, sound, vol, SoundCategory.AMBIENT);
        }
    }

    public static void playWorldSound(World w, String sound) {
        for (Player p : w.getPlayers()) {
            playCustomSound(p, sound, SoundCategory.AMBIENT);
        }
    }

    public static void playWorldSound(World w, String sound, float volume) {
        for (Player p : w.getPlayers()) {
            playCustomSound(p, sound, volume, SoundCategory.MASTER);
        }
    }
}
