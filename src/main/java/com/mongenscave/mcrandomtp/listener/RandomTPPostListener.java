package com.mongenscave.mcrandomtp.listener;

import com.mongenscave.mcrandomtp.api.event.RandomTPPostEvent;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.util.ColorUtil;
import com.mongenscave.mcrandomtp.util.SoundUtil;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RandomTPPostListener implements Listener {

    @EventHandler
    public void onTeleport(@NotNull RandomTPPostEvent event) {
        Player player = event.getPlayer();

        if (Config.getBoolean("teleport.success.sound.enabled")) {
            String name = Config.getString("teleport.success.sound.name");
            float pitch = (float) Config.getDouble("teleport.success.sound.pitch");
            float volume = (float) Config.getDouble("teleport.success.sound.volume");

            SoundUtil.playSound(player, name, volume, pitch);
        }

        String type = Config.getString("teleport.success.notify.type");
        String message = Config.getString("teleport.success.notify.message");
        String subtitle = Config.getString("teleport.success.notify.subtitle");

        String rendered = message.replace("%s", player.getName());
        String renderedSub = subtitle.replace("%s", player.getName());

        switch (type.toLowerCase()) {
            case "none" -> {
            }
            case "title" -> player.sendTitle(ColorUtil.process(rendered), ColorUtil.process(renderedSub), 0, 40, 10);
            case "message" -> player.sendMessage(ColorUtil.process(rendered));
            default -> player.sendActionBar(ColorUtil.process(rendered));
        }

        String particleName = Config.getString("teleport.success.particle");
        if (!particleName.isEmpty()) {
            try {
                Particle particle = Particle.valueOf(particleName);
                player.getWorld().spawnParticle(particle, event.getLocation().clone().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}