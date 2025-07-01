package com.mongenscave.mcrandomtp.util;

import com.mongenscave.mcrandomtp.McRandomTP;
import com.mongenscave.mcrandomtp.config.Config;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class LoggerUtil {

    private final Logger logger = LogManager.getLogger("McRandomTP");

    public void info(@NotNull String msg, @NotNull Object... objs) {
        logger.info(msg, objs);
    }

    public void warn(@NotNull String msg, @NotNull Object... objs) {
        logger.warn(msg, objs);
    }

    public void error(@NotNull String msg, @NotNull Object... objs) {
        logger.error(msg, objs);
    }

    public void debug(@NotNull String msg, @NotNull Object... objs) {
        if (Config.getBoolean("debug")) {
            info(msg, objs);
        }
    }

    public void printStartup() {
        String main = "\u001B[38;5;149m";
        String yellow = "\u001B[33m";
        String reset = "\u001B[0m";
        String software = McRandomTP.getInstance().getServer().getName();
        String version = McRandomTP.getInstance().getServer().getVersion();

        info(" ");
        info("{}    ____      _    _   _ ____   ___  __  __ _____ ____  {}", main, reset);
        info("{}   |  _ \\    / \\  | \\ | |  _ \\ / _ \\|  \\/  |_   _|  _ \\ {}", main, reset);
        info("{}   | |_) |  / _ \\ |  \\| | | | | | | | |\\/| | | | | |_) |{}", main, reset);
        info("{}   |  _ <  / ___ \\| |\\  | |_| | |_| | |  | | | | |  __/ {}", main, reset);
        info("{}   |_| \\_\\/_/   \\_\\_| \\_|____/ \\___/|_|  |_| |_| |_|    {}", main, reset);
        info(" ");
        info("{}   The plugin successfully started.{}", main, reset);
        info("{}   mc-RandomTP {} {}{}", main, software, version, reset);
        info("{}   Discord @ dc.mongenscave.com{}", yellow, reset);
        info(" ");
    }
}