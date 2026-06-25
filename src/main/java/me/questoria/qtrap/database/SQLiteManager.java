package me.questoria.qtrap.database;

import me.questoria.qtrap.QTrapPlugin;

import java.io.File;

public final class SQLiteManager extends DatabaseManager {
    public SQLiteManager(QTrapPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String jdbcUrl() {
        File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.sqlite-file", "qtrap.db"));
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }
}
