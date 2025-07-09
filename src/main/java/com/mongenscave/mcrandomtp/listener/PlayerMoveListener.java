package com.mongenscave.mcrandomtp.listener;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.manager.TeleportManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final TeleportManager teleportManager = McRandomTP.getInstance().getTeleportManager();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Config.getBoolean("teleport.delay.cancel-on-move")) return;
        if (!teleportManager.isWaiting(event.getPlayer())) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        teleportManager.cancel(event.getPlayer());
    }
}