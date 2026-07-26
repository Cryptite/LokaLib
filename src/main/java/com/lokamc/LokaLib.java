package com.lokamc;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class LokaLib extends JavaPlugin {
    public static LokaLib instance;
    public static final Logger log = Logger.getLogger("LokaLib");

    public static LokaLib getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
