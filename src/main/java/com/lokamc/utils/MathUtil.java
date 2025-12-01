package com.lokamc.utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {
    private static final TreeMap<Integer, String> map = new TreeMap<>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    public static String toRoman(int number) {
        if (number == 0) return "";

        int l = map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number - l);
    }

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
        float rand = ThreadLocalRandom.current().nextFloat() * total;

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

        float rand = ThreadLocalRandom.current().nextFloat();
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

    public static float getPercent(double v1, double v2) {
        if (v1 == 0) return 0;

        int percent = (int) ((v1 / v2) * 100);
        return percent == 0 ? 1 : percent;
    }

    public static float getPercent(float v1, float v2) {
        if (v1 == 0) return 0;

        int percent = (int) ((v1 / v2) * 100);
        return percent == 0 ? 1 : percent;
    }

    public static float getPercent(int v1, int v2) {
        if (v1 == 0) return 0;

        int percent = (int) (((float) v1 / (float) v2) * 100);
        return percent == 0 ? 1 : percent;
    }
}
