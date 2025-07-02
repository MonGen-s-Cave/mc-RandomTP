package com.mongenscave.mcrandomtp.guis;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.config.GuiConfig;
import com.mongenscave.mcrandomtp.config.Messages;
import com.mongenscave.mcrandomtp.guis.builder.GuiItemBuilder;
import com.mongenscave.mcrandomtp.manager.TeleportManager;
import com.mongenscave.mcrandomtp.util.ColorUtil;
import com.mongenscave.mcrandomtp.util.LoggerUtil;
import com.mongenscave.mcrandomtp.util.SoundUtil;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MainGUI {

    private static final TeleportManager manager = McRandomTP.getInstance().getTeleportManager();

    public static void open(Player player) {
        Section configSection = GuiConfig.getSection("main-gui");
        if (configSection == null) {
            LoggerUtil.warn("Main GUI config not found!");
            return;
        }

        int rows = configSection.getInt("rows", 3);
        String title = GuiItemBuilder.applyPlaceholders(player, configSection.getString("title", "&8Main GUI"));

        Gui gui = Gui.gui()
                .rows(rows)
                .title(Component.text(ColorUtil.process(title)))
                .disableAllInteractions()
                .create();

        GuiItemBuilder.addDecorativeItems(gui, GuiConfig.get());

        Section itemsSection = configSection.getSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getRoutesAsStrings(false)) {
                Section itemSection = itemsSection.getSection(key);
                if (itemSection == null) continue;

                ItemStack item = GuiItemBuilder.fromConfig(itemSection);
                String worldName;
                List<String> actions = itemSection.getStringList("actions");
                worldName = actions.stream().filter(action -> action.toLowerCase().startsWith("[rtp]")).map(action -> action.split(" ")).filter(parts -> parts.length >= 2).findFirst().map(parts -> parts[1]).orElse(null);

                if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    List<String> newLore = item.getItemMeta().getLore().stream()
                            .map(line -> GuiItemBuilder.applyGuiPlaceholders(line, worldName))
                            .toList();

                    var meta = item.getItemMeta();
                    meta.setLore(newLore);
                    item.setItemMeta(meta);
                }


                List<Integer> slots = GuiItemBuilder.getSlotList(GuiConfig.get(), "main-gui.items." + key + ".slot");

                GuiItem guiItem = new GuiItem(item, event -> {
                    event.setCancelled(true);
                    Player clicker = (Player) event.getWhoClicked();
                    for (String action : actions) {
                        handleAction(clicker, action);
                    }
                });

                for (int slot : slots) {
                    gui.setItem(slot, guiItem);
                }
            }
        }

        gui.open(player);
    }

    private static void handleAction(Player player, String rawAction) {
        if (rawAction == null || rawAction.isBlank()) return;

        String trimmed = rawAction.trim();
        var matcher = java.util.regex.Pattern.compile("\\[(.+)]\\s*(.*)").matcher(trimmed);

        if (!matcher.matches()) {
            LoggerUtil.warn("§cInvalid action format: " + trimmed);
            return;
        }

        String actionType = matcher.group(1).toLowerCase();
        String argument = matcher.group(2).trim();

        switch (actionType) {
            case "rtp" -> {
                if (argument.isEmpty()) {
                    LoggerUtil.warn("[rtp] action requires a world name.");
                    return;
                }
                var world = Bukkit.getWorld(argument);
                if (world == null) {
                    player.sendMessage(Messages.get("messages.rtp-command.world-not-found").replace("%world%", argument));
                    return;
                }
                manager.teleportPlayer(player, world);
            }

            case "sound" -> {
                if (argument.isEmpty()) {
                    LoggerUtil.warn("[sound] action requires a sound name.");
                    return;
                }
                try {
                    var sound = Sound.valueOf(argument.toUpperCase());
                    SoundUtil.playSound(player, sound.name(), 1.0f, 1.0f);
                } catch (IllegalArgumentException e) {
                    LoggerUtil.warn("Invalid sound: " + argument);
                }
            }

            case "close" -> player.closeInventory();

            case "message" -> {
                if (!argument.isEmpty()) {
                    player.sendMessage(GuiItemBuilder.applyPlaceholders(player, argument));
                }
            }

            case "command" -> {
                if (!argument.isEmpty()) {
                    Bukkit.dispatchCommand(player, argument);
                }
            }

            default -> LoggerUtil.warn("Unknown action type: [" + actionType + "]");
        }
    }
}