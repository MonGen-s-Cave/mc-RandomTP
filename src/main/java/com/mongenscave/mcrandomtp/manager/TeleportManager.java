package com.mongenscave.mcrandomtp.manager;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.config.Messages;
import com.mongenscave.mcrandomtp.task.TeleportTask;
import com.mongenscave.mcrandomtp.util.ColorUtil;
import com.mongenscave.mcrandomtp.util.SoundUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TeleportManager {

    private static final Map<UUID, MyScheduledTask> delayTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, TeleportTask> activeTeleports = new ConcurrentHashMap<>();

    public void teleportPlayer(Player player, World world) {
        if (activeTeleports.containsKey(player.getUniqueId())) {
            player.sendMessage(Messages.get("messages.rtp-command.already-teleporting"));
            return;
        }

        if (Config.getBoolean("teleport.delay.enabled")) {
            startDelayedTeleport(player, world);
        } else {
            startTeleport(player, world);
        }
    }

    public boolean isWaiting(Player player) {
        return delayTasks.containsKey(player.getUniqueId());
    }

    public void cancel(Player player) {
        UUID uuid = player.getUniqueId();
        MyScheduledTask task = delayTasks.remove(uuid);
        if (task != null) {
            task.cancel();
            player.sendMessage(Messages.get("messages.rtp-command.cancelled-by-move"));
        }
    }

    private void startTeleport(Player player, World world) {
        UUID uuid = player.getUniqueId();

        TeleportTask task = new TeleportTask(player, world, () -> {
            activeTeleports.remove(uuid);
        });

        activeTeleports.put(uuid, task);

        task.start(() -> activeTeleports.put(uuid, task));
    }

    private void startDelayedTeleport(Player player, World world) {
        AtomicReference<MyScheduledTask> taskRef = new AtomicReference<>();

        int delay = Config.getInt("teleport.delay.seconds");
        String type = Config.getString("teleport.delay.notify.type");
        String msg = Config.getString("teleport.delay.notify.message");
        String subtitle = Config.getString("teleport.delay.notify.subtitle");
        boolean playSound = Config.getBoolean("teleport.delay.sound.enabled");

        String soundName = Config.getString("teleport.delay.sound.name");
        float pitch = (float) Config.getDouble("teleport.delay.sound.pitch");
        float volume = (float) Config.getDouble("teleport.delay.sound.volume");

        UUID uuid = player.getUniqueId();

        final int[] secondsLeft = {delay};
        MyScheduledTask task = McRandomTP.getScheduler().runTaskTimer(() -> {
            if (!player.isOnline()) {
                cancel(player);
                return;
            }

            if (secondsLeft[0] <= 0) {
                delayTasks.remove(uuid);
                taskRef.get().cancel();

                TeleportTask teleportTask = new TeleportTask(player, world, () -> {
                    activeTeleports.remove(uuid);
                });

                teleportTask.start(() -> activeTeleports.put(uuid, teleportTask));
                return;
            }

            String rendered = msg.replace("%s", String.valueOf(secondsLeft[0]));

            switch (type.toLowerCase()) {
                case "none" -> {}
                case "title" -> {
                    String renderedSubtitle = subtitle.replace("%s", String.valueOf(secondsLeft[0]));
                    player.sendTitle(ColorUtil.process(rendered), ColorUtil.process(renderedSubtitle), 0, 25, 5);
                }
                case "message" -> player.sendMessage(ColorUtil.process(rendered));
                default -> player.sendActionBar(ColorUtil.process(rendered));
            }

            if (playSound) {
                SoundUtil.playSound(player, soundName, volume, pitch);
            }

            secondsLeft[0]--;
        }, 0L, 20L);

        taskRef.set(task);
        delayTasks.put(uuid, task);
    }

    public void shutdown() {
        delayTasks.values().forEach(MyScheduledTask::cancel);
        delayTasks.clear();

        activeTeleports.values().forEach(TeleportTask::cancel);
        activeTeleports.clear();
    }
}