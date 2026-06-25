package me.questoria.qtrap.database;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.model.TrapChunk;
import me.questoria.qtrap.model.TrapMember;
import me.questoria.qtrap.model.TrapModel;
import me.questoria.qtrap.model.TrapRole;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class DatabaseManager {
    protected final QTrapPlugin plugin;

    protected DatabaseManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract String jdbcUrl();

    protected String username() {
        return null;
    }

    protected String password() {
        return null;
    }

    protected Connection connection() throws SQLException {
        if (username() == null) {
            return DriverManager.getConnection(jdbcUrl());
        }
        return DriverManager.getConnection(jdbcUrl(), username(), password());
    }

    public synchronized void init() {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS qtrap_traps (
                      id VARCHAR(64) PRIMARY KEY,
                      owner_uuid VARCHAR(36),
                      name VARCHAR(128) NOT NULL,
                      level INT NOT NULL,
                      health INT NOT NULL,
                      max_health INT NOT NULL,
                      bank_balance DOUBLE NOT NULL,
                      for_sale BOOLEAN NOT NULL,
                      sale_price DOUBLE NOT NULL,
                      pvp BOOLEAN NOT NULL,
                      visit BOOLEAN NOT NULL,
                      spawn TEXT,
                      created_at BIGINT NOT NULL,
                      updated_at BIGINT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS qtrap_chunks (
                      trap_id VARCHAR(64) NOT NULL,
                      world VARCHAR(128) NOT NULL,
                      chunk_x INT NOT NULL,
                      chunk_z INT NOT NULL,
                      PRIMARY KEY (world, chunk_x, chunk_z)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS qtrap_members (
                      trap_id VARCHAR(64) NOT NULL,
                      uuid VARCHAR(36) NOT NULL,
                      role VARCHAR(32) NOT NULL,
                      PRIMARY KEY (trap_id, uuid)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS qtrap_trusted (
                      trap_id VARCHAR(64) NOT NULL,
                      uuid VARCHAR(36) NOT NULL,
                      permission VARCHAR(32) NOT NULL,
                      PRIMARY KEY (trap_id, uuid, permission)
                    )
                    """);
        } catch (SQLException exception) {
            throw new IllegalStateException("QTrap database init failed", exception);
        }
    }

    public CompletableFuture<List<TrapModel>> loadTraps() {
        return CompletableFuture.supplyAsync(() -> {
            List<TrapModel> traps = new ArrayList<>();
            try (Connection connection = connection();
                 PreparedStatement trapsStatement = connection.prepareStatement("SELECT * FROM qtrap_traps");
                 ResultSet resultSet = trapsStatement.executeQuery()) {
                while (resultSet.next()) {
                    TrapModel trap = new TrapModel(resultSet.getString("id"));
                    String owner = resultSet.getString("owner_uuid");
                    if (owner != null && !owner.isBlank()) {
                        UUID ownerId = uuid(owner);
                        if (ownerId != null) {
                            trap.owner(ownerId);
                        }
                    }
                    trap.name(resultSet.getString("name"));
                    trap.level(resultSet.getInt("level"));
                    trap.health(resultSet.getInt("health"));
                    trap.maxHealth(resultSet.getInt("max_health"));
                    trap.bankBalance(resultSet.getDouble("bank_balance"));
                    trap.forSale(resultSet.getBoolean("for_sale"));
                    trap.salePrice(resultSet.getDouble("sale_price"));
                    trap.pvp(resultSet.getBoolean("pvp"));
                    trap.visit(resultSet.getBoolean("visit"));
                    trap.spawn(TrapModel.deserializeLocation(resultSet.getString("spawn")));
                    trap.createdAt(resultSet.getLong("created_at"));
                    trap.updatedAt(resultSet.getLong("updated_at"));
                    loadChildren(connection, trap);
                    traps.add(trap);
                }
            } catch (SQLException exception) {
                plugin.getLogger().severe("Trap verileri yuklenemedi: " + exception.getMessage());
            }
            return traps;
        }, plugin.databaseExecutor());
    }

    public CompletableFuture<Void> saveTrap(TrapModel trap) {
        return CompletableFuture.runAsync(() -> saveTrapNow(trap), plugin.databaseExecutor());
    }

    public CompletableFuture<Void> deleteTrap(String id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = connection()) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM qtrap_traps WHERE id=?")) {
                    statement.setString(1, id);
                    statement.executeUpdate();
                }
                deleteChildren(connection, id);
            } catch (SQLException exception) {
                plugin.getLogger().severe("Trap silinemedi: " + exception.getMessage());
            }
        }, plugin.databaseExecutor());
    }

    public void saveTrapNow(TrapModel trap) {
        try (Connection connection = connection()) {
            ensureSchema(connection);
            try (PreparedStatement statement = connection.prepareStatement("""
                    REPLACE INTO qtrap_traps
                    (id, owner_uuid, name, level, health, max_health, bank_balance, for_sale, sale_price, pvp, visit, spawn, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setString(1, trap.id());
                statement.setString(2, trap.owner() == null ? null : trap.owner().toString());
                statement.setString(3, trap.name());
                statement.setInt(4, trap.level());
                statement.setInt(5, trap.health());
                statement.setInt(6, trap.maxHealth());
                statement.setDouble(7, trap.bankBalance());
                statement.setBoolean(8, trap.forSale());
                statement.setDouble(9, trap.salePrice());
                statement.setBoolean(10, trap.pvp());
                statement.setBoolean(11, trap.visit());
                statement.setString(12, TrapModel.serializeLocation(trap.spawn()));
                statement.setLong(13, trap.createdAt());
                statement.setLong(14, trap.updatedAt());
                statement.executeUpdate();
            }
            deleteChildren(connection, trap.id());
            saveChildren(connection, trap);
        } catch (SQLException exception) {
            plugin.getLogger().severe("Trap kaydedilemedi: " + exception.getMessage());
        }
    }

    public void close() {
    }

    private void ensureSchema(Connection connection) throws SQLException {
        init();
    }

    private void loadChildren(Connection connection, TrapModel trap) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM qtrap_chunks WHERE trap_id=?")) {
            statement.setString(1, trap.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    trap.chunks().add(new TrapChunk(resultSet.getString("world"), resultSet.getInt("chunk_x"), resultSet.getInt("chunk_z")));
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM qtrap_members WHERE trap_id=?")) {
            statement.setString(1, trap.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID uuid = uuid(resultSet.getString("uuid"));
                    if (uuid != null) {
                        trap.members().put(uuid, new TrapMember(uuid, role(resultSet.getString("role"))));
                    }
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM qtrap_trusted WHERE trap_id=?")) {
            statement.setString(1, trap.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID uuid = uuid(resultSet.getString("uuid"));
                    if (uuid != null) {
                        trap.trusted().computeIfAbsent(uuid, key -> new LinkedHashSet<>()).add(resultSet.getString("permission"));
                    }
                }
            }
        }
    }

    private void saveChildren(Connection connection, TrapModel trap) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO qtrap_chunks (trap_id, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?)")) {
            for (TrapChunk chunk : trap.chunks()) {
                statement.setString(1, trap.id());
                statement.setString(2, chunk.world());
                statement.setInt(3, chunk.x());
                statement.setInt(4, chunk.z());
                statement.addBatch();
            }
            statement.executeBatch();
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO qtrap_members (trap_id, uuid, role) VALUES (?, ?, ?)")) {
            for (TrapMember member : trap.memberValues()) {
                statement.setString(1, trap.id());
                statement.setString(2, member.uuid().toString());
                statement.setString(3, member.role().name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO qtrap_trusted (trap_id, uuid, permission) VALUES (?, ?, ?)")) {
            for (var entry : trap.trusted().entrySet()) {
                UUID uuid = entry.getKey();
                Set<String> permissions = entry.getValue();
                for (String permission : permissions) {
                    statement.setString(1, trap.id());
                    statement.setString(2, uuid.toString());
                    statement.setString(3, permission);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void deleteChildren(Connection connection, String id) throws SQLException {
        for (String table : List.of("qtrap_chunks", "qtrap_members", "qtrap_trusted")) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE trap_id=?")) {
                statement.setString(1, id);
                statement.executeUpdate();
            }
        }
    }

    private TrapRole role(String raw) {
        try {
            return TrapRole.valueOf(raw);
        } catch (RuntimeException exception) {
            return TrapRole.MEMBER;
        }
    }

    private UUID uuid(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
