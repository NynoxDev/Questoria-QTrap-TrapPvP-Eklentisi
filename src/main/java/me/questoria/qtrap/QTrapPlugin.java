package me.questoria.qtrap;

import me.questoria.qtrap.command.CommandManager;
import me.questoria.qtrap.config.ConfigManager;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.database.DatabaseManager;
import me.questoria.qtrap.database.MySQLManager;
import me.questoria.qtrap.database.SQLiteManager;
import me.questoria.qtrap.gui.GuiManager;
import me.questoria.qtrap.hologram.HologramManager;
import me.questoria.qtrap.hook.PlaceholderHook;
import me.questoria.qtrap.hook.VaultHook;
import me.questoria.qtrap.listener.ListenerManager;
import me.questoria.qtrap.trap.TrapManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class QTrapPlugin extends JavaPlugin {
    private ExecutorService databaseExecutor;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private TrapManager trapManager;
    private GuiManager guiManager;
    private HologramManager hologramManager;
    private VaultHook vaultHook;
    private PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.databaseExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "QTrap-Database");
            thread.setDaemon(true);
            return thread;
        });
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.vaultHook = new VaultHook(this);
        this.databaseManager = createDatabaseManager();
        try {
            this.databaseManager.init();
        } catch (IllegalStateException exception) {
            if (!"mysql".equalsIgnoreCase(getConfig().getString("database.type", "sqlite"))) {
                throw exception;
            }
            String reason = exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage();
            getLogger().warning("MySQL baglantisi kurulamadigi icin SQLite moduna geciliyor: " + reason);
            this.databaseManager = new SQLiteManager(this);
            this.databaseManager.init();
        }
        this.trapManager = new TrapManager(this);
        this.trapManager.loadAll().join();
        this.guiManager = new GuiManager(this);
        this.hologramManager = new HologramManager(this);
        if (getConfig().getBoolean("holograms.update-on-load", true)) {
            this.hologramManager.updateAll();
        }

        CommandManager commandManager = new CommandManager(this);
        PluginCommand trap = Objects.requireNonNull(getCommand("trap"));
        trap.setExecutor(commandManager);
        trap.setTabCompleter(commandManager);
        PluginCommand qtrap = Objects.requireNonNull(getCommand("qtrap"));
        qtrap.setExecutor(commandManager);
        qtrap.setTabCompleter(commandManager);
        PluginCommand trapachunk = Objects.requireNonNull(getCommand("trapachunk"));
        trapachunk.setExecutor(commandManager);
        trapachunk.setTabCompleter(commandManager);

        new ListenerManager(this).register();
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") && getConfig().getBoolean("placeholders.enabled", true)) {
            this.placeholderHook = new PlaceholderHook(this);
            this.placeholderHook.register();
        }
        getLogger().info("QTrap aktif edildi. Yuklenen trap: " + trapManager.traps().size());
    }

    @Override
    public void onDisable() {
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }
        if (hologramManager != null && getConfig().getBoolean("holograms.delete-on-disable", true)) {
            hologramManager.deleteAll();
        }
        if (trapManager != null) {
            trapManager.saveAll().join();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (databaseExecutor != null) {
            databaseExecutor.shutdownNow();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        configManager.reload();
        messageManager.reload();
        trapManager.reloadCache();
        hologramManager.reload();
        hologramManager.updateAll();
    }

    private DatabaseManager createDatabaseManager() {
        String type = getConfig().getString("database.type", "sqlite");
        if ("mysql".equalsIgnoreCase(type)) {
            return new MySQLManager(this);
        }
        return new SQLiteManager(this);
    }

    public ExecutorService databaseExecutor() {
        return databaseExecutor;
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public MessageManager messages() {
        return messageManager;
    }

    public DatabaseManager database() {
        return databaseManager;
    }

    public TrapManager traps() {
        return trapManager;
    }

    public GuiManager gui() {
        return guiManager;
    }

    public HologramManager holograms() {
        return hologramManager;
    }

    public VaultHook vault() {
        return vaultHook;
    }
}
