package com.mongenscave.mcrandomtp.util;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.command.RTPAdminCommand;
import com.mongenscave.mcrandomtp.command.RTPCommand;
import com.mongenscave.mcrandomtp.handler.CommandExceptionHandler;
import com.mongenscave.mcrandomtp.listener.PlayerMoveListener;
import com.mongenscave.mcrandomtp.listener.RandomTPPostListener;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.orphan.Orphans;

@UtilityClass
public class RegisterUtil {

    private final McRandomTP plugin = McRandomTP.getInstance();

    public void registerListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerMoveListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new RandomTPPostListener(), plugin);
    }

    public void registerCommands() {
        var lamp = BukkitLamp.builder(plugin)
                .exceptionHandler(new CommandExceptionHandler())
                .build();

        lamp.register(Orphans.path("rtp", "randomtp", "randomteleport", "mc-rtp").handler(new RTPCommand()));
        lamp.register(Orphans.path("rtpadmin", "randomteleportadmin", "mc-rtpadmin", "mcrtpadmin").handler(new RTPAdminCommand()));
    }
}