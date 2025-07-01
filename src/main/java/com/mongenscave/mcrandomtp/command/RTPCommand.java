package com.mongenscave.mcrandomtp.command;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.config.Messages;
import com.mongenscave.mcrandomtp.manager.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class RTPCommand implements OrphanCommand {

    private final TeleportManager manager = McRandomTP.getInstance().getTeleportManager();

    @CommandPlaceholder
    @CommandPermission("mcrandomtp.use")
    @Usage("/alias [world]")
    public void rtp(@NotNull Player player, @Optional String worldName) {
        if (worldName == null) worldName = Config.getString("teleport.default-world");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(Messages.get("messages.rtp-command.world-not-found").replace("%world%", worldName));
            return;
        }

        manager.teleportPlayer(player, world);
    }
}