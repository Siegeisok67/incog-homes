package com.incogdev.homes.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static String color(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public static String fill(String raw, Map<String, String> placeholders) {
        String result = raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    public static void send(CommandSender sender, String raw) {
        sender.sendMessage(color(raw));
    }

    public static void send(CommandSender sender, String raw, Map<String, String> placeholders) {
        sender.sendMessage(color(fill(raw, placeholders)));
    }
}
