package me.questoria.qtrap.config;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.model.TrapRole;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

public final class ConfigManager {
    private final QTrapPlugin plugin;

    public ConfigManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
    }

    public int defaultHealth() {
        return plugin.getConfig().getInt("defaults.trap-health", 2000);
    }

    public int maxHealthForLevel(int level) {
        return plugin.getConfig().getInt("levels." + level + ".max-health", plugin.getConfig().getInt("defaults.max-trap-health", 2000));
    }

    public double upgradePrice(int level) {
        return plugin.getConfig().getDouble("levels." + level + ".upgrade-price", -1D);
    }

    public int maxMembers(int level) {
        return plugin.getConfig().getInt("levels." + level + ".max-members", 3);
    }

    public int chunkLimit(int level) {
        return plugin.getConfig().getInt("levels." + level + ".chunk-limit", 1);
    }

    public double bankLimit(int level) {
        return plugin.getConfig().getDouble("levels." + level + ".bank-limit", 1_000_000D);
    }

    public int maxConfiguredLevel() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("levels");
        if (section == null) {
            return 1;
        }
        int max = 1;
        for (String key : section.getKeys(false)) {
            try {
                max = Math.max(max, Integer.parseInt(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return max;
    }

    public boolean roleHas(TrapRole role, String permission) {
        Set<String> permissions = new HashSet<>(plugin.getConfig().getStringList("roles." + role.name() + ".permissions"));
        return permissions.contains(permission);
    }
}
