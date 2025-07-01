package com.mongenscave.mcrandomtp.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class RandomTPPreEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter private final Player player;
    @Getter private final World world;
    private boolean cancelled = false;

    public RandomTPPreEvent(Player player, World world) {
        this.player = player;
        this.world = world;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}