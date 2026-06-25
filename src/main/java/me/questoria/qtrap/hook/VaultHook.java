package me.questoria.qtrap.hook;

import me.questoria.qtrap.QTrapPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultHook {
    private final QTrapPlugin plugin;
    private Economy economy;

    public VaultHook(QTrapPlugin plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            return;
        }
        RegisteredServiceProvider<Economy> registration = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration != null) {
            economy = registration.getProvider();
        }
    }

    public boolean available() {
        return economy != null;
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0D) {
            return true;
        }
        if (economy == null) {
            return !plugin.getConfig().getBoolean("economy.require-vault", true);
        }
        if (!economy.has(player, amount)) {
            return false;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (economy != null && amount > 0D) {
            economy.depositPlayer(player, amount);
        }
    }

    public String format(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        return String.format("%.2f", amount);
    }
}
