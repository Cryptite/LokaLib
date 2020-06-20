package com.lokamc;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LokaLib extends JavaPlugin {
    public static ExecutorService configFileExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
