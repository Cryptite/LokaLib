package com.lokamc.utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {
    public static <T> T rollMap(Map<T, Float> map) {
        float total = 0;
        Map<T, Float> cumulativeMap = new HashMap<>();
        for (T o : map.keySet()) {
            float chance = map.get(o);
            cumulativeMap.put(o, chance + total);
            total += chance;
        }

        //Sort ranges from highest to lowest
        List<Map.Entry<T, Float>> entries = new ArrayList<>(cumulativeMap.entrySet());
        entries.sort(Map.Entry.comparingByValue());

        //Next roll from 0 to maxValue
        float rand = new Random().nextFloat() * total;

        //Then iterate through the descending list of entries, first one that's <= the random roll is our guy.
        for (Map.Entry<T, Float> entry : entries) {
            if (rand <= entry.getValue()) return entry.getKey();
        }

        return (T) map.entrySet().iterator().next();
    }

    /**
     * Returns true if dropRate > random 0-100
     *
     * @param dropRate Number between 1 and 100
     * @return
     */
    public static boolean rollChance(double dropRate) {
        if (dropRate == 100) return true;
        else if (dropRate == 0) return false;

        float rand = new Random().nextFloat();
        double chance = rand * 100;
        return dropRate > chance;
    }

    public static int getRandomBetween(int[] range) {
        return getRandomBetween(range[0], range[1]);
    }

    public static int getRandomBetween(int min, int max) {
        return min != max ? ThreadLocalRandom.current().nextInt((max - min) + 1) + min : min;
    }

    public static float getRandomBetween(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    /**
     * Return a value normalized between
     *
     * @param valueIn
     * @param baseMin
     * @param baseMax
     * @param limitMin
     * @param limitMax
     * @return
     */
    public static float normalize(final float valueIn, final float baseMin, final float baseMax, final float limitMin, final float limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }
}
