package com.mongenscave.mcrandomtp.api;

import com.mongenscave.mcrandomtp.task.TeleportTask;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class McRandomTPAPI {

    public static void teleport(@NotNull Player player, @NotNull World world) {
        TeleportTask task = new TeleportTask(player, world, () -> {});
        task.start(() -> {});
    }

    public static void teleport(@NotNull Player player, @NotNull World world, @NotNull Runnable onStart) {
        TeleportTask task = new TeleportTask(player, world, () -> {});
        task.start(onStart);
    }

}