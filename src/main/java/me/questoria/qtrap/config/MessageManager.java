package me.questoria.qtrap.config;

import me.questoria.qtrap.QTrapPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MessageManager {
    private final QTrapPlugin plugin;
    private FileConfiguration messages;

    public MessageManager(QTrapPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, new LinkedHashMap<>());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String prefix = messages.getString("messages.prefix", "");
        if (messages.isList("messages." + key)) {
            for (String line : messages.getStringList("messages." + key)) {
                sender.sendMessage(color(apply(line, placeholders)));
            }
            return;
        }
        String message = messages.getString("messages." + key, key);
        sender.sendMessage(color(apply(prefix + message, placeholders)));
    }

    public String raw(String key, Map<String, String> placeholders) {
        return color(apply(messages.getString("messages." + key, key), placeholders));
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
