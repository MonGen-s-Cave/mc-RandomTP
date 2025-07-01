package com.mongenscave.mcrandomtp.config;

import com.mongenscave.mcrandomtp.util.ColorUtil;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class Messages {

    private static YamlDocument messages;

    public static void load(@NotNull JavaPlugin plugin) {
        try {
            File file = new File(plugin.getDataFolder(), "messages.yml");
            messages = YamlDocument.create(
                    file,
                    Objects.requireNonNull(plugin.getResource("messages.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("version")).build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.yml", e);
        }
    }

    public static void reload() {
        try {
            if (messages != null) messages.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Contract(pure = true)
    public static @NotNull String get(@NotNull String key) {
        if (messages == null) return ColorUtil.process("§c[!] Messages not initialized");

        String value = messages.getString(key, "§cUnknown message key: " + key);
        if (value.contains("%prefix%")) {
            String prefix = messages.getString("prefix", "");
            value = value.replace("%prefix%", prefix);
        }

        return ColorUtil.process(value);
    }
}