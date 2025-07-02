package com.mongenscave.mcrandomtp.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class GuiConfig {

    private static YamlDocument config;

    private GuiConfig() {}

    public static void load(@NotNull JavaPlugin plugin) {
        try {
            config = YamlDocument.create(
                    new File(plugin.getDataFolder(), "guis.yml"),
                    Objects.requireNonNull(plugin.getResource("guis.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("version")).build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.yml", e);
        }
    }

    public static void set(String path, Object value) {
        if (config != null) {
            config.set(path, value);
        }
    }

    public static void reload() {
        try {
            if (config != null) config.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            if (config != null) config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static @NotNull String getString(@NotNull String path) {
        return config.getString(path);
    }

    public static @NotNull List<String> getStringList(@NotNull String path) {
        return config.getStringList(path);
    }

    public static boolean getBoolean(@NotNull String path) {
        return config.getBoolean(path);
    }

    public static int getInt(@NotNull String path) {
        return config.getInt(path);
    }

    public static double getDouble(@NotNull String path) {
        return config.getDouble(path);
    }

    public static long getLong(@NotNull String path) {
        return config.getLong(path);
    }

    public static Section getSection(@NotNull String path) {
        return config.getSection(path);
    }

    public static YamlDocument get() {
        return config;
    }

}