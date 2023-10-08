package com.lokamc.utils;

import com.lokamc.LokaLib;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

import static com.lokamc.utils.LocationUtil.getSphereClosestPoint;
import static com.lokamc.utils.MathUtil.normalize;
import static net.kyori.adventure.sound.Sound.Source.AMBIENT;
import static net.kyori.adventure.sound.Sound.Source.MASTER;

public class SoundUtil {

    public static final int NEARBY_SOUND_MAX_DISTANCE = 15 * 15; //15 blocks
    public static final int DISTANT_SOUND_MAX_DISTANCE = 2000 * 2000; //2000 blocks

    public static void playCustomSound(Player p, Location l, String sound, Source source, float volume) {
        //Also play for the player
        Sound adventureSound = Sound.sound(Key.key(sound.toLowerCase()), source, volume, 1);
        p.playSound(adventureSound, l.getX(), l.getY(), l.getZ());
    }

    public static void playCustomSound(Player p, String sound, Source source) {
        playCustomSound(p, sound, source, 1f);
    }

    public static void playCustomSound(Player p, String sound, Source source, float volume) {
        if (p == null) return;

        Sound adventureSound = Sound.sound(Key.key(sound.toLowerCase()), source, volume, 1);
        p.playSound(adventureSound, Sound.Emitter.self());
    }

    public static void playWorldCustomSound(Location l, String sound, Source source, float volume) {
        playWorldCustomSound(l, sound, source, 15, volume);
    }

    public static void playWorldCustomSound(Entity e, String sound, Source source) {
        playWorldCustomSound(e.getLocation(), sound, source, 15, 1);
    }

    public static void playWorldCustomSound(Entity e, String sound, Source source, float volume) {
        playWorldCustomSound(e.getLocation(), sound, source, 15, volume);
    }

    public static void playWorldCustomSound(Location l, String sound, Source source) {
        playWorldCustomSound(l, sound, source, 15, 1);
    }

    public static void playWorldCustomSound(Location l, String sound, Source source, int radius, float volume) {
        if (Bukkit.isPrimaryThread()) {
            playWorldSound(l, sound, source, radius, volume);
        } else {
            Bukkit.getScheduler().runTask(LokaLib.getInstance(), () -> playWorldSound(l, sound, source, radius, volume));
        }
    }

    private static void playWorldSound(Location l, String sound, Source source, int radius, float volume) {
        //Get all nearby entities within radius blocks
        Sound adventureSound = Sound.sound(Key.key(sound.toLowerCase()), source, volume, 1);
        for (Player p : l.getNearbyPlayers(radius)) {
            p.playSound(adventureSound, l.getX(), l.getY(), l.getZ());
        }
    }

    public static void playWorldCustomSound(Collection<Player> players, Location l, String sound, Source source, float volume) {
        //Get all nearby entities within radius blocks
        Sound adventureSound = Sound.sound(Key.key(sound.toLowerCase()), source, volume, 1);
        for (Player p : players) {
            p.playSound(adventureSound, l.getX(), l.getY(), l.getZ());
        }
    }

    public static void playDistantSound(Location l, String sound) {
        playDistantSound(l, sound, MASTER, .5f, 1.5f);
    }

    public static void playDistantSound(Location l, String sound, float minVol, float maxVol) {
        playDistantSound(l, sound, MASTER, minVol, maxVol);
    }

    public static void playDistantSound(Location l, String sound, Sound.Source source, float minVol, float maxVol) {
        for (Player p : l.getWorld().getPlayers()) {
            double distance = p.getLocation().distanceSquared(l);
            if (distance <= NEARBY_SOUND_MAX_DISTANCE) {
                playCustomSound(p, l, sound, source, maxVol);
            } else if (distance <= DISTANT_SOUND_MAX_DISTANCE) {
                float volume = maxVol - normalize((float) distance, 0, DISTANT_SOUND_MAX_DISTANCE, minVol, maxVol);
                playCustomSound(p, getSphereClosestPoint(p.getLocation(), 14, l), sound, source, volume);
            }
        }
    }

    public static void playWorldSound(World w, String sound) {
        for (Player p : w.getPlayers()) {
            playCustomSound(p, sound, AMBIENT);
        }
    }

    public static void playWorldSound(World w, String sound, float volume) {
        for (Player p : w.getPlayers()) {
            playCustomSound(p, sound, MASTER, volume);
        }
    }
}
