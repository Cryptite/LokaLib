package com.lokamc.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FutureUtils {
    private static final ExecutorService utilsExecutor = Executors.newFixedThreadPool(5);

    public static void runAsyncThenSync(Plugin plugin, Runnable async, Runnable sync) {
        tryAsync(async).thenRun(() -> Bukkit.getScheduler().runTask(plugin, sync));
    }

    public static void thenRunSync(Plugin plugin, CompletableFuture future, Runnable sync) {
        future.thenRun(() -> Bukkit.getScheduler().runTask(plugin, sync));
    }

    public static <T> void thenAcceptSync(Plugin plugin, CompletableFuture<T> future, Consumer<T> sync) {
        future.thenAcceptAsync(object -> Bukkit.getScheduler().runTask(plugin, () -> sync.accept(object)));
    }

    public static CompletableFuture<Void> tryAsync(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, utilsExecutor);
    }
}
