package com.lokamc.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class CollectionUtils {
    public static <T> T getRandomFrom(List<T> list, Predicate<T> exclude) {
        if (list.size() == 1) {
            return list.iterator().next();
        }

        T object = getRandomFrom(list);
        while (!exclude.test(object)) {
            object = getRandomFrom(list);
        }

        return object;
    }

    public static <T> T getRandomFrom(T[] list) {
        return list[(ThreadLocalRandom.current().nextInt(list.length))];
    }

    public static <T> T getRandomFrom(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
