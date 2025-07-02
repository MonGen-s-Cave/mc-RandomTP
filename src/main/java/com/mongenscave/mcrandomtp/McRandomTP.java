package com.mongenscave.mcrandomtp;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.config.GuiConfig;
import com.mongenscave.mcrandomtp.config.Messages;
import com.mongenscave.mcrandomtp.manager.TeleportManager;
import com.mongenscave.mcrandomtp.util.LoggerUtil;
import com.mongenscave.mcrandomtp.util.RegisterUtil;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class McRandomTP extends JavaPlugin {

    @Getter private static McRandomTP instance;
    @Getter private TeleportManager teleportManager;
    @Getter private static TaskScheduler scheduler;

    @Override
    public void onLoad() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
    }

    @Override
    public void onEnable() {
        Config.load(this);
        Messages.load(this);
        GuiConfig.load(this);

        this.teleportManager = new TeleportManager();

        RegisterUtil.registerListeners();
        RegisterUtil.registerCommands();

        LoggerUtil.printStartup();
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) teleportManager.shutdown();
    }
}