package com.mongenscave.mcrandomtp.api.event;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RandomTPPostEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter private final Player player;
    @Getter private final Location location;

    public RandomTPPostEvent(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}