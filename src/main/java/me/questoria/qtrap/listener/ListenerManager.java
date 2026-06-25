package me.questoria.qtrap.listener;

import me.questoria.qtrap.QTrapPlugin;

public final class ListenerManager {
    private final QTrapPlugin plugin;

    public ListenerManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(new ProtectionListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(plugin), plugin);
    }
}
