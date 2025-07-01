package com.mongenscave.mcrandomtp.handler;

import com.mongenscave.mcrandomtp.config.Messages;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.node.ParameterNode;

public class CommandExceptionHandler extends BukkitExceptionHandler {

    @Override
    public void onSenderNotPlayer(SenderNotPlayerException exception, @NotNull BukkitCommandActor actor) {
        actor.error(Messages.get("messages.not-player"));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException exception, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        actor.error(Messages.get("messages.missing-argument"));
    }

    @Override
    public void onNoPermission(@NotNull NoPermissionException exception, @NotNull BukkitCommandActor actor) {
        actor.error(Messages.get("messages.no-permission"));
    }
}