package com.lokamc;

import com.lokamc.utils.ClickConfirmation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LokaLib extends JavaPlugin {
    public static final ExecutorService configFileExecutor = Executors.newSingleThreadExecutor();
    public static LokaLib instance;

    public static LokaLib getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        getCommand("confirm").setExecutor((commandSender, command, s, args) -> {
            if (commandSender instanceof Player && s.equalsIgnoreCase("confirm")) {
                ClickConfirmation.getInstance().respondConsumer((Player) commandSender, args);
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
