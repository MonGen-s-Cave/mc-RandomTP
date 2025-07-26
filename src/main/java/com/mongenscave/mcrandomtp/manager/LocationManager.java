package com.mongenscave.mcrandomtp.manager;

import com.mongenscave.mcrandomtp.config.Config;
import org.bukkit.*;
import org.bukkit.block.Block;
import io.papermc.lib.PaperLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class LocationManager {
    private static final int PLAYER_HEIGHT = 2;
    private static final double CENTER_OFFSET = 0.5;

    private static final Set<Material> BLACKLISTED_BLOCKS = Set.of(
            Material.LAVA, Material.CACTUS, Material.MAGMA_BLOCK, Material.FIRE, Material.WATER
    );

    public static @NotNull CompletableFuture<Location> findSafeLocation(@NotNull World world, double radius, int minY, int maxY, int maxTries) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        Location center = world.getWorldBorder().getCenter();
        runAttempt(world, center, radius, minY, maxY, maxTries, 0, future);
        return future;
    }

    private static void runAttempt(World world, Location center, double radius, int minY, int maxY,
                                   int maxTries, int attempt, CompletableFuture<Location> future) {
        if (attempt >= maxTries) {
            if (Config.getBoolean("use-fallback-location")) future.complete(getFallbackLocation(world));
            else future.complete(null);

            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();
        int x = centerX + random.nextInt((int) -radius, (int) radius + 1);
        int z = centerZ + random.nextInt((int) -radius, (int) radius + 1);

        PaperLib.getChunkAtAsync(world, x >> 4, z >> 4, true).thenAccept(chunk -> {
            Location candidate = findSafeLocationInChunk(world, x, z, minY, maxY);
            if (candidate != null) future.complete(candidate);
            else runAttempt(world, center, radius, minY, maxY, maxTries, attempt + 1, future);

        }).exceptionally(ex -> {
            runAttempt(world, center, radius, minY, maxY, maxTries, attempt + 1, future);
            return null;
        });
    }

    private static @Nullable Location findSafeLocationInChunk(@NotNull World world, int x, int z, int minY, int maxY) {
        World.Environment environment = world.getEnvironment();

        switch (environment) {
            case NORMAL -> {
                int highestY = world.getHighestBlockYAt(x, z);
                if (highestY < minY || highestY > maxY) return null;

                Location candidate = new Location(world, x + CENTER_OFFSET, highestY + 1, z + CENTER_OFFSET);
                return isLocationSafe(candidate) ? candidate : null;
            }

            case NETHER -> {
                return findSafeLocationInRange(world, x, z, minY, maxY);
            }

            case THE_END -> {
                return findSafeLocationInEnd(world, x, z, minY, maxY);
            }

            default -> {
                return null;
            }
        }
    }

    private static @Nullable Location findSafeLocationInRange(World world, int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            Location candidate = new Location(world, x + CENTER_OFFSET, y, z + CENTER_OFFSET);
            if (isLocationSafe(candidate)) return candidate;
        }

        return null;
    }

    private static Location findSafeLocationInEnd(@NotNull World world, int x, int z, int minY, int maxY) {
        int highestY = world.getHighestBlockYAt(x, z);

        if (highestY > 0 && highestY >= minY && highestY <= maxY) {
            Location candidate = new Location(world, x + CENTER_OFFSET, highestY + 1, z + CENTER_OFFSET);
            if (isLocationSafe(candidate)) return candidate;
        }

        return findSafeLocationInRange(world, x, z, minY, maxY);
    }

    private static boolean isLocationSafe(@NotNull Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Block groundBlock = world.getBlockAt(x, y - 1, z);
        if (!groundBlock.getType().isSolid() || isBlockUnsafe(groundBlock, true)) return false;

        for (int i = 0; i < PLAYER_HEIGHT; i++) {
            Block block = world.getBlockAt(x, y + i, z);
            if (isBlockUnsafe(block, false) || block.getType().isSolid()) return false;
        }

        World.Environment environment = world.getEnvironment();

        if (environment == World.Environment.NETHER) return !hasNearbyLava(world, x, y, z);
        else if (environment == World.Environment.THE_END) return y > 0 && groundBlock.getType() != Material.AIR;

        return true;
    }

    private static boolean hasNearbyLava(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    if (block.getType() == Material.LAVA) return true;
                }
            }
        }
        return false;
    }

    private static boolean isBlockUnsafe(@NotNull Block block, boolean isGround) {
        Material material = block.getType();

        if (BLACKLISTED_BLOCKS.contains(material)) return true;
        if (isGround) return !material.isSolid();


        return false;
    }

    public static @NotNull Location getFallbackLocation(@NotNull World world) {
        String path = "worlds." + world.getName() + ".fallback";
        double x = Config.getDouble(path + ".x");
        double y = Config.getDouble(path + ".y");
        double z = Config.getDouble(path + ".z");
        float yaw = (float) Config.getDouble(path + ".yaw");
        float pitch = (float) Config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }
}