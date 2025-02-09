package com.lokamc.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lokamc.LokaLib;
import com.lokamc.types.StringChunk;
import com.lokamc.types.StringLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FutureUtils {
    private static final LokaLib plugin = LokaLib.instance;
    public static final ExecutorService utilsExecutor = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("Loka FutureUtils").build());
    private static final Map<String, BukkitTask> tryLaterTasks = new HashMap<>();

    public static void tryFuture(CompletableFuture<Boolean> future, Runnable onSuccess) {
        tryFuture(future, onSuccess, null);
    }

    public static void tryFuture(CompletableFuture<Boolean> future, Runnable onSuccess, Runnable onFailure) {
        future.handle((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                result = false;
            }

            if (result) {
                onSuccess.run();
            } else if (onFailure != null) {
                onFailure.run();
            }
            return null;
        });
    }

    public static <T> void tryFuture(CompletableFuture<T> future, Consumer<T> onSuccess) {
        tryFuture(future, onSuccess, null);
    }

    public static <T> void tryFuture(CompletableFuture<T> future, Consumer<T> onSuccess, Runnable onFailure) {
        future.handle((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                result = null;
            }

            if (result != null) {
                onSuccess.accept(result);
            } else if (onFailure != null) {
                onFailure.run();
            }
            return null;
        });
    }

    public static void tryFuture(Player p, CompletableFuture<Boolean> future, Consumer<Player> onSuccess) {
        tryFuture(p, future, onSuccess, null);
    }

    public static void tryFutureSync(Player p, CompletableFuture<Boolean> future, Consumer<Player> onSuccess) {
        tryFutureSync(p, future, onSuccess, null);
    }

    public static void tryFuture(Player p, CompletableFuture<Boolean> future, Consumer<Player> onSuccess, Consumer<Player> onFailure) {
        future.handle((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                result = false;
            }

            performFuture(p, result, onSuccess, onFailure);
            return null;
        });
    }

    public static void tryFutureSync(Player p, CompletableFuture<Boolean> future, Consumer<Player> onSuccess, Consumer<Player> onFailure) {
        future.handleAsync((result, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        result = false;
                    }

                    performFuture(p, result, onSuccess, onFailure);
                    return null;
                },
                Bukkit.getScheduler().getMainThreadExecutor(plugin));
    }

    private static void performFuture(Player p, boolean result, Consumer<Player> onSuccess, Consumer<Player> onFailure) {
        if (result) {
            onSuccess.accept(Bukkit.getPlayer(p.getUniqueId()));
        } else if (onFailure != null) {
            onFailure.accept(Bukkit.getPlayer(p.getUniqueId()));
        }
    }

    public static void runSyncIfOnline(Player p, Consumer<Player> consumer) {
        runSyncIfOnline(p.getUniqueId(), consumer);
    }

    public static void runSyncIfOnline(UUID uuid, Consumer<Player> consumer) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                consumer.accept(player);
            }
        });
    }

    public static void runAsyncIfOnline(Player p, Consumer<Player> consumer) {
        runAsyncIfOnline(p.getUniqueId(), consumer);
    }

    public static void runAsyncIfOnline(UUID uuid, Consumer<Player> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                consumer.accept(player);
            }
        });
    }

    public static void runLaterSyncIfOnline(Player p, Consumer<Player> consumer, int ticks) {
        runLaterSyncIfOnline(p.getUniqueId(), consumer, ticks);
    }

    public static void runLaterSyncIfOnline(UUID uuid, Consumer<Player> consumer, int ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                consumer.accept(player);
            }
        }, ticks);
    }

    public static void runLaterAsyncIfOnline(Player p, Consumer<Player> consumer, int ticks) {
        runLaterAsyncIfOnline(p.getUniqueId(), consumer, ticks);
    }

    public static void runLaterAsyncIfOnline(UUID uuid, Consumer<Player> consumer, int ticks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                consumer.accept(player);
            }
        }, ticks);
    }

    public static void tryLaterTask(String key, Runnable runnable, int ticks) {
        BukkitTask task = tryLaterTasks.get(key);
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tryLaterTasks.remove(key);
            runnable.run();
        }, ticks);

        tryLaterTasks.put(key, task);
    }

    public static void tryLaterAsyncTask(String key, Runnable runnable, int ticks) {
        BukkitTask task = tryLaterTasks.get(key);
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            tryLaterTasks.remove(key);
            runnable.run();
        }, ticks);

        tryLaterTasks.put(key, task);
    }

    public static void runAsyncThenSync(Runnable async, Runnable sync) {
        tryAsync(async).thenRun(() -> Bukkit.getScheduler().runTask(plugin, sync));
    }

    public static void thenRunSync(CompletableFuture future, Runnable sync) {
        future.thenRun(() -> Bukkit.getScheduler().runTask(plugin, sync));
    }

    public static <T> void thenAcceptSync(CompletableFuture<T> future, Consumer<T> sync) {
        future.thenAcceptAsync(object -> Bukkit.getScheduler().runTask(plugin, () -> sync.accept(object)));
    }

    public static CompletableFuture<Void> tryAsync(Runnable runnable) {
        return tryAsync(runnable, null);
    }

    public static CompletableFuture<Void> tryAsync(Runnable runnable, ExecutorService executorService) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executorService != null ? executorService : utilsExecutor);
    }

    public static void runOnceLoaded(Collection<StringLocation> locations, Runnable runnable) {
        Set<StringChunk> chunks = locations.stream().map(location -> location.getStringChunk()).collect(Collectors.toSet());
        runOnceRegionLoaded(chunks, runnable);
    }

    public static void runOnceRegionLoaded(Collection<StringChunk> stringChunks, Runnable runnable) {
        tryAsync(() -> {
            Iterator<StringChunk> iterator = stringChunks.iterator();
            Set<Chunk> chunks = new HashSet<>();
            while (iterator.hasNext()) {
                StringChunk c = iterator.next();
                if (c.isLoaded()) {
                    Chunk chunk = c.getChunk();
                    chunks.add(chunk);
                    iterator.remove();
                } else {
                    c.getChunkAsync()
                            .thenAcceptAsync(chunk -> Bukkit.getScheduler().runTask(plugin, () -> {
                                chunks.add(chunk);
                                chunk.addPluginChunkTicket(plugin);
                                stringChunks.remove(c);
                            }));
                }
            }

            while (!stringChunks.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bukkit.getScheduler().runTaskLater(plugin,
                        () -> chunks.forEach(chunk -> chunk.removePluginChunkTicket(plugin)),
                        20 * 3);
            });
        });
    }

    public static void runTaskOrImmediately(boolean immediately, Runnable runnable) {
        if (immediately) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runTaskAsyncOrImmediately(boolean immediately, Runnable runnable) {
        if (immediately) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void runTaskLaterOrImmediately(boolean immediately, Runnable runnable, int ticks) {
        if (immediately) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
        }
    }

    public static void runTaskLaterAsyncOrImmediately(boolean immediately, Runnable runnable, int ticks) {
        if (immediately) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticks);
        }
    }
}
