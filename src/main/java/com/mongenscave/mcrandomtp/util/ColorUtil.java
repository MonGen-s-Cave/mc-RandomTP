package com.mongenscave.mcrandomtp.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@SuppressWarnings("deprecation")
public class ColorUtil {
    private final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public String process(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String color = matcher.group(1);
            String replacement = String.format("\u00A7x\u00A7%c\u00A7%c\u00A7%c\u00A7%c\u00A7%c\u00A7%c",
                    color.charAt(0), color.charAt(1), color.charAt(2),
                    color.charAt(3), color.charAt(4), color.charAt(5));
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}