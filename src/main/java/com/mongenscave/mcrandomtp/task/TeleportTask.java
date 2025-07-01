package com.mongenscave.mcrandomtp.task;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.api.event.RandomTPPostEvent;
import com.mongenscave.mcrandomtp.api.event.RandomTPPreEvent;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.config.Messages;
import com.mongenscave.mcrandomtp.util.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class TeleportTask {

    private final Player player;
    private final World world;

    private final Runnable onComplete;

    private final int maxTries;
    private final double radius;
    private final int minY;
    private final int maxY;

    private int attempt = 0;
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
        tryLocation();
    }

    private void tryLocation() {
        double x = ThreadLocalRandom.current().nextDouble(-radius, radius);
        double z = ThreadLocalRandom.current().nextDouble(-radius, radius);
        Location base = world.getWorldBorder().getCenter().clone().add(x, 0, z);

        LoggerUtil.debug("Attempt {}: Generated base location X:{}, Z:{} (World: {})",
                attempt + 1, base.getBlockX(), base.getBlockZ(), world.getName());

        world.getChunkAtAsync(base).thenAccept(this::processChunk).exceptionally(ex -> {
            LoggerUtil.warn("Chunk load failed during teleport attempt {} for {}: {}", attempt + 1, player.getName(), ex.getMessage());
            retryOrFail();
            return null;
        });
    }

    private void processChunk(@NotNull Chunk chunk) {
        int cx = ThreadLocalRandom.current().nextInt(16);
        int cz = ThreadLocalRandom.current().nextInt(16);
        int y = chunk.getChunkSnapshot().getHighestBlockYAt(cx, cz);

        LoggerUtil.debug("Processing chunk for {}: inner-chunk position x={}, z={}, topY={}",
                player.getName(), cx, cz, y);

        if (y < minY || y > maxY) {
            LoggerUtil.debug("Position rejected: Y={} is outside allowed bounds ({}–{})", y, minY, maxY);
            retryOrFail();
            return;
        }

        Block ground = chunk.getBlock(cx, y, cz);
        Block above = ground.getRelative(0, 1, 0);

        if (ground.isSolid() && !above.isSolid()) {
            Location target = ground.getLocation().add(0, 1, 0);

            completed = true;
            onComplete.run();

            LoggerUtil.debug("Valid location found for {}: X={}, Y={}, Z={}", player.getName(),
                    target.getBlockX(), target.getBlockY(), target.getBlockZ());

            player.teleportAsync(target).thenRun(() -> McRandomTP.getScheduler().runTask(() -> Bukkit.getPluginManager().callEvent(new RandomTPPostEvent(player, target))));
        } else {
            LoggerUtil.debug("Rejected: unsafe surface at Y={} (ground={}, above={})", y, ground.getType(), above.getType());
            retryOrFail();
        }
    }

    private void retryOrFail() {
        attempt++;
        if (attempt >= maxTries) {
            LoggerUtil.warn("Random teleport failed for {} after {} attempts.", player.getName(), attempt);
            McRandomTP.getScheduler().runTask(() ->
                    player.sendMessage(Messages.get("messages.rtp-command.failed"))
            );
            onComplete.run();
        } else {
            LoggerUtil.debug("Retrying teleportation for {} (attempt {}/{})", player.getName(), attempt + 1, maxTries);
            tryLocation();
        }
    }

    public void cancel() {
        completed = true;
    }
}