package com.mongenscave.mcrandomtp.command;

import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.config.GuiConfig;
import com.mongenscave.mcrandomtp.config.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class RTPAdminCommand implements OrphanCommand {

    @Subcommand("reload")
    @CommandPermission("mcrandomtp.admin.reload")
    @Usage("/alias reload")
    public void reload(@NotNull CommandSender sender) {
        Config.reload();
        Messages.reload();
        GuiConfig.reload();

        sender.sendMessage(Messages.get("messages.admin.reload-command.success"));
    }

    @Subcommand("debug")
    @CommandPermission("mcrandomtp.admin.debug")
    @Usage("/alias debug")
    public void debug(@NotNull CommandSender sender) {
        if (Config.getBoolean("debug")) {
            sender.sendMessage(Messages.get("messages.admin.debug-command.disabled"));
            Config.set("debug", false);
        } else {
            sender.sendMessage(Messages.get("messages.admin.debug-command.enabled"));
            Config.set("debug", true);
        }
        Config.save();
        Config.reload();
    }
}