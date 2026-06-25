package me.questoria.qtrap.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.model.TrapModel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderHook extends PlaceholderExpansion {
    private final QTrapPlugin plugin;

    public PlaceholderHook(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "qtrap";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Questoria";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        TrapModel trap = plugin.traps().memberTrap(player.getUniqueId()).orElse(null);
        if (trap == null) {
            return "";
        }
        return switch (params.toLowerCase()) {
            case "owner" -> trap.owner() == null ? "-" : ownerName(trap);
            case "name" -> trap.name();
            case "level" -> String.valueOf(trap.level());
            case "health" -> String.valueOf(trap.health());
            case "max_health" -> String.valueOf(trap.maxHealth());
            case "bank" -> plugin.vault().format(trap.bankBalance());
            case "members" -> String.valueOf(trap.members().size());
            case "max_members" -> String.valueOf(plugin.configManager().maxMembers(trap.level()));
            case "pvp" -> trap.pvp() ? "Acik" : "Kapali";
            case "visit" -> trap.visit() ? "Acik" : "Kapali";
            case "price" -> plugin.vault().format(trap.salePrice());
            default -> null;
        };
    }

    private String ownerName(TrapModel trap) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(trap.owner());
        return owner.getName() == null ? trap.owner().toString().substring(0, 8) : owner.getName();
    }
}
