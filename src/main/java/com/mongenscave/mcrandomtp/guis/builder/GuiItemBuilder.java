package com.mongenscave.mcrandomtp.guis.builder;

import com.mongenscave.mcrandomtp.config.Config;
import com.mongenscave.mcrandomtp.util.ColorUtil;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class GuiItemBuilder {

    public static ItemStack fromConfig(Section section) {
        if (section == null || !section.contains("material")) return new ItemStack(Material.STONE);

        String materialId = section.getString("material", "STONE");

        Material material = Material.getMaterial(materialId.toUpperCase());
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (section.contains("name")) {
            meta.setDisplayName(ColorUtil.process(section.getString("name")));
        }

        if (section.contains("lore")) {
            List<String> rawLore = section.getStringList("lore");
            meta.setLore(ColorUtil.process(rawLore));
        }

        if (section.contains("custom-model-data")) {
            meta.setCustomModelData(section.getInt("custom-model-data"));
        }

        if (section.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
        }

        if (section.contains("item-flags")) {
            List<String> flags = section.getStringList("item-flags");
            for (String flagStr : flags) {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(flagStr.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (section.contains("nbt-tags")) {
            Section tags = section.getSection("nbt-tags");
            if (tags != null) {
                for (Object keyObj : tags.getKeys()) {
                    String key = keyObj.toString();
                    String value = tags.getString(key);
                    if (value != null) {
                        container.set(NamespacedKey.minecraft(key), PersistentDataType.STRING, value);
                    }
                }
            }
        }

        if (section.contains("nbt-ints")) {
            Section ints = section.getSection("nbt-ints");
            if (ints != null) {
                for (Object keyObj : ints.getKeys()) {
                    String key = keyObj.toString();
                    int value = ints.getInt(key);
                    container.set(NamespacedKey.minecraft(key), PersistentDataType.INTEGER, value);
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    public static void addDecorativeItems(Gui gui, YamlDocument config) {
        if (!config.contains("decorative-items")) return;

        for (String key : config.getSection("decorative-items").getRoutesAsStrings(false)) {
            String path = "decorative-items." + key;
            ItemStack item = fromConfig(config.getSection(path));
            for (int slot : getSlotList(config, path + ".slot")) {
                gui.setItem(slot, new GuiItem(item, event -> event.setCancelled(true)));
            }
        }
    }

    public static List<Integer> getSlotList(YamlDocument config, String path) {
        if (config.isList(path)) {
            return config.getIntList(path);
        } else if (config.isInt(path)) {
            return Collections.singletonList(config.getInt(path));
        }
        return Collections.emptyList();
    }

    public static String applyPlaceholders(Player player, String input) {
        if (player == null || input == null) return input;

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Method method = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                return (String) method.invoke(null, player, input);
            }
        } catch (Exception ignored) {}
        return input;
    }

    public static String applyGuiPlaceholders(String input, String worldName) {
        if (input == null) return "";

        String result = input;

        if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double radius = Config.getDouble("worlds." + worldName + ".radius");
                long players = world.getPlayers().stream().filter(Player::isOnline).count();

                result = result
                        .replace("%radius%", String.valueOf((int) radius))
                        .replace("%players%", String.valueOf(players));
            }
        }

        return ColorUtil.process(result);
    }
}