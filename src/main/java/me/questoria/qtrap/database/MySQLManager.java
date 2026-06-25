package me.questoria.qtrap.database;

import me.questoria.qtrap.QTrapPlugin;

public final class MySQLManager extends DatabaseManager {
    public MySQLManager(QTrapPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String jdbcUrl() {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "qtrap");
        String params = plugin.getConfig().getString("database.mysql.params", "?useSSL=false");
        return "jdbc:mysql://" + host + ":" + port + "/" + database + params;
    }

    @Override
    protected String username() {
        return plugin.getConfig().getString("database.mysql.username", "root");
    }

    @Override
    protected String password() {
        return plugin.getConfig().getString("database.mysql.password", "");
    }
}
