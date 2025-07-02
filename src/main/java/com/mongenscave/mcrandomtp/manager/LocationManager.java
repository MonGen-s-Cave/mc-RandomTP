package com.mongenscave.mcrandomtp.manager;

import com.mongenscave.mcrandomtp.config.Config;
import org.bukkit.*;
import org.bukkit.block.Block;
import io.papermc.lib.PaperLib;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class LocationManager {

    private static final int PLAYER_HEIGHT = 2;
    private static final double CENTER_OFFSET = 0.5;

    private static final Set<Material> BLACKLISTED_BLOCKS = Set.of(
            Material.LAVA, Material.CACTUS, Material.MAGMA_BLOCK, Material.FIRE, Material.WATER
    );

    public static CompletableFuture<Location> findSafeLocation(World world, double radius, int minY, int maxY, int maxTries) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        Location center = world.getWorldBorder().getCenter();
        runAttempt(world, center, radius, minY, maxY, maxTries, 0, future);
        return future;
    }

    private static void runAttempt(World world, Location center, double radius, int minY, int maxY,
                                   int maxTries, int attempt, CompletableFuture<Location> future) {
        if (attempt >= maxTries) {
            if (Config.getBoolean("use-fallback-location")) {
                future.complete(getFallbackLocation(world));
            } else {
                future.complete(null);
            }
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();
        int x = centerX + random.nextInt((int) -radius, (int) radius + 1);
        int z = centerZ + random.nextInt((int) -radius, (int) radius + 1);

        PaperLib.getChunkAtAsync(world, x >> 4, z >> 4, true).thenAccept(chunk -> {
            int highestY = world.getHighestBlockYAt(x, z);
            if (highestY < minY || highestY > maxY) {
                runAttempt(world, center, radius, minY, maxY, maxTries, attempt + 1, future);
                return;
            }

            Location candidate = new Location(world, x + CENTER_OFFSET, highestY + 1, z + CENTER_OFFSET);
            if (isLocationSafe(candidate)) {
                future.complete(candidate);
            } else {
                runAttempt(world, center, radius, minY, maxY, maxTries, attempt + 1, future);
            }
        }).exceptionally(ex -> {
            runAttempt(world, center, radius, minY, maxY, maxTries, attempt + 1, future);
            return null;
        });
    }

    private static boolean isLocationSafe(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Block groundBlock = world.getBlockAt(x, y - 1, z);
        if (isBlockUnsafe(groundBlock, true)) return false;

        for (int i = 0; i < PLAYER_HEIGHT; i++) {
            Block block = world.getBlockAt(x, y + i, z);
            if (isBlockUnsafe(block, false)) return false;
        }

        return true;
    }

    private static boolean isBlockUnsafe(Block block, boolean isGround) {
        Material material = block.getType();
        return BLACKLISTED_BLOCKS.contains(material) || (isGround != material.isSolid());
    }

    public static Location getFallbackLocation(World world) {
        String path = "worlds." + world.getName() + ".fallback";
        double x = Config.getDouble(path + ".x");
        double y = Config.getDouble(path + ".y");
        double z = Config.getDouble(path + ".z");
        float yaw = (float) Config.getDouble(path + ".yaw");
        float pitch = (float) Config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

}