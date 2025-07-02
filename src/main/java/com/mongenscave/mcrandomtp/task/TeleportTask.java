package com.mongenscave.mcrandomtp.task;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.api.event.RandomTPPostEvent;
import com.mongenscave.mcrandomtp.api.event.RandomTPPreEvent;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.config.Messages;
import com.mongenscave.mcrandomtp.manager.LocationManager;
import com.mongenscave.mcrandomtp.util.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeleportTask {

    private final Player player;
    private final World world;
    private final Runnable onComplete;

    private final int maxTries;
    private final double radius;
    private final int minY;
    private final int maxY;

    private boolean completed = false;

    public TeleportTask(@NotNull Player player, @NotNull World world, @NotNull Runnable onComplete) {
        this.player = player;
        this.world = world;
        this.onComplete = onComplete;

        final String w = world.getName();
        this.radius = Config.getDouble("worlds." + w + ".radius");
        this.minY = Config.getInt("worlds." + w + ".min-y");
        this.maxY = Config.getInt("worlds." + w + ".max-y");
        this.maxTries = Config.getInt("teleport.max-tries");
    }

    public void start(@NotNull Runnable onStart) {
        if (completed) return;
        LoggerUtil.debug("Starting random teleportation for {} in world '{}'.", player.getName(), world.getName());

        RandomTPPreEvent event = new RandomTPPreEvent(player, world);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            LoggerUtil.debug("Teleportation for {} was cancelled by another plugin.", player.getName());
            return;
        }

        onStart.run();

        LocationManager.findSafeLocation(world, radius, minY, maxY, maxTries)
                .thenAccept(location -> {
                    if (location == null) {
                        LoggerUtil.warn("Random teleport failed for {} after {} attempts.", player.getName(), maxTries);
                        McRandomTP.getScheduler().runTask(() -> {
                            player.sendMessage(Messages.get("messages.rtp-command.failed"));
                            onComplete.run();
                        });
                        return;
                    }

                    LoggerUtil.debug("Valid location found for {}: X={}, Y={}, Z={}",
                            player.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());

                    player.teleportAsync(location).thenAccept(success -> {
                        McRandomTP.getScheduler().runTask(() -> {
                            onComplete.run();
                            if (success) {
                                Bukkit.getPluginManager().callEvent(new RandomTPPostEvent(player, location));
                            } else {
                                player.sendMessage(Messages.get("messages.rtp-command.failed"));
                            }
                        });
                    });
                });

    }

    public void cancel() {
        completed = true;
    }
}