package me.questoria.qtrap.config;

import me.questoria.qtrap.QTrapPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MessageManager {
    private final QTrapPlugin plugin;

    public MessageManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, new LinkedHashMap<>());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        if (plugin.getConfig().isList("messages." + key)) {
            for (String line : plugin.getConfig().getStringList("messages." + key)) {
                sender.sendMessage(color(apply(line, placeholders)));
            }
            return;
        }
        String message = plugin.getConfig().getString("messages." + key, key);
        sender.sendMessage(color(apply(prefix + message, placeholders)));
    }

    public String raw(String key, Map<String, String> placeholders) {
        return color(apply(plugin.getConfig().getString("messages." + key, key), placeholders));
    }

    public static Map<String, String> placeholders(Object... values) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            placeholders.put(String.valueOf(values[i]), String.valueOf(values[i + 1]));
        }
        return placeholders;
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static String apply(String text, Map<String, String> placeholders) {
        String result = text == null ? "" : text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
