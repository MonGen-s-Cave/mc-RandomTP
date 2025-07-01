package com.mongenscave.mcrandomtp.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@UtilityClass
public class SoundUtil {
    public void playSound(Player player, String soundName, float volume, float pitch) {
        Location loc = player.getLocation();

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            player.playSound(loc, soundName, volume, pitch);
        }
    }
}