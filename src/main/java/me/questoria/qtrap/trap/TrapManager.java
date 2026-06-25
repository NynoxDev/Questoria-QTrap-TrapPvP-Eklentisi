package me.questoria.qtrap.trap;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.model.TrapChunk;
import me.questoria.qtrap.model.TrapMember;
import me.questoria.qtrap.model.TrapModel;
import me.questoria.qtrap.model.TrapRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TrapManager {
    private final QTrapPlugin plugin;
    private final Map<String, TrapModel> traps = new LinkedHashMap<>();
    private final Map<String, TrapModel> chunkCache = new LinkedHashMap<>();
    private final Map<UUID, String> invites = new LinkedHashMap<>();
    private final Map<UUID, Boolean> trapChat = new LinkedHashMap<>();
    private final Map<UUID, String> flyTrap = new LinkedHashMap<>();

    public TrapManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> loadAll() {
        return plugin.database().loadTraps().thenAccept(loaded -> {
            traps.clear();
            for (TrapModel trap : loaded) {
                traps.put(trap.id().toLowerCase(), trap);
            }
            rebuildChunkCache();
        });
    }

    public CompletableFuture<Void> saveAll() {
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for (TrapModel trap : traps.values()) {
            futures.add(plugin.database().saveTrap(trap));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public void reloadCache() {
        rebuildChunkCache();
    }

    public Collection<TrapModel> traps() {
        return traps.values();
    }

    public Optional<TrapModel> byId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(traps.get(id.toLowerCase()));
    }

    public Optional<TrapModel> at(Location location) {
        if (location == null || location.getWorld() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(chunkCache.get(TrapChunk.of(location.getChunk()).key()));
    }

    public Optional<TrapModel> at(TrapChunk chunk) {
        if (chunk == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(chunkCache.get(chunk.key()));
    }

    public Optional<TrapModel> ownedBy(UUID uuid) {
        return traps.values().stream().filter(trap -> uuid.equals(trap.owner())).findFirst();
    }

    public Optional<TrapModel> memberTrap(UUID uuid) {
        return traps.values().stream().filter(trap -> trap.isMember(uuid)).findFirst();
    }

    public TrapModel create(String id, double price, Chunk chunk) {
        return create(id, price, chunk, null);
    }

    public TrapModel create(String id, double price, Location creatorLocation) {
        return create(id, price, creatorLocation.getChunk(), creatorLocation);
    }

    private TrapModel create(String id, double price, Chunk chunk, Location creatorLocation) {
        TrapModel trap = new TrapModel(id);
        trap.level(plugin.getConfig().getInt("defaults.start-level", 1));
        trap.health(plugin.configManager().defaultHealth());
        trap.maxHealth(plugin.configManager().maxHealthForLevel(trap.level()));
        trap.bankBalance(plugin.getConfig().getDouble("defaults.bank-balance", 0D));
        trap.salePrice(price);
        trap.pvp(plugin.getConfig().getBoolean("defaults.pvp", false));
        trap.visit(plugin.getConfig().getBoolean("defaults.visit", false));
        trap.chunks().add(TrapChunk.of(chunk));
        if (creatorLocation != null) {
            trap.spawn(creatorLocation);
        }
        traps.put(id.toLowerCase(), trap);
        rebuildChunkCache();
        plugin.database().saveTrap(trap);
        log(trap, (UUID) null, "CREATE", "Trap oluşturuldu. Fiyat: " + price);
        plugin.holograms().update(trap);
        return trap;
    }

    public void delete(TrapModel trap) {
        log(trap, (UUID) null, "DELETE", "Trap silindi.");
        plugin.holograms().delete(trap);
        traps.remove(trap.id().toLowerCase());
        rebuildChunkCache();
        plugin.database().deleteTrap(trap.id());
    }

    public boolean buy(Player player, TrapModel trap, double price) {
        if (trap.owned() && !trap.forSale()) {
            return false;
        }
        if (!plugin.vault().available() && plugin.getConfig().getBoolean("economy.require-vault", true)) {
            plugin.messages().send(player, "no-economy");
            return false;
        }
        if (!plugin.vault().withdraw(player, price)) {
            plugin.messages().send(player, "not-enough-money", MessageManager.placeholders("%price%", price));
            return false;
        }
        if (trap.owner() != null && trap.forSale()) {
            OfflinePlayer oldOwner = Bukkit.getOfflinePlayer(trap.owner());
            plugin.vault().deposit(oldOwner, price);
        }
        trap.owner(player.getUniqueId());
        trap.members().clear();
        trap.trusted().clear();
        trap.forSale(false);
        trap.salePrice(price);
        trap.health(trap.maxHealth());
        plugin.database().saveTrap(trap);
        log(trap, player, "BUY", "Trap satın alındı. Fiyat: " + price);
        plugin.holograms().update(trap);
        plugin.visuals().playBuyEffect(player, trap);
        return true;
    }

    public boolean hasPermission(TrapModel trap, UUID uuid, String permission) {
        TrapRole role = trap.role(uuid);
        if (role != null && plugin.configManager().roleHas(role, permission)) {
            return true;
        }
        return trap.trusted().getOrDefault(uuid, java.util.Set.of()).contains(permission);
    }

    public void invite(Player inviter, Player target, TrapModel trap) {
        invites.put(target.getUniqueId(), trap.id());
        plugin.messages().send(inviter, "invited", MessageManager.placeholders("%player%", target.getName()));
        plugin.messages().send(target, "invite-received", MessageManager.placeholders("%trap%", trap.name()));
    }

    public boolean acceptInvite(Player player) {
        String trapId = invites.remove(player.getUniqueId());
        if (trapId == null) {
            return false;
        }
        TrapModel trap = byId(trapId).orElse(null);
        if (trap == null) {
            return false;
        }
        if (trap.members().size() >= plugin.configManager().maxMembers(trap.level())) {
            return false;
        }
        trap.members().put(player.getUniqueId(), new TrapMember(player.getUniqueId(), TrapRole.MEMBER));
        plugin.database().saveTrap(trap);
        log(trap, player, "MEMBER_JOIN", "Davet kabul edildi.");
        plugin.holograms().update(trap);
        return true;
    }

    public boolean denyInvite(Player player) {
        return invites.remove(player.getUniqueId()) != null;
    }

    public void kick(TrapModel trap, UUID uuid) {
        trap.members().remove(uuid);
        plugin.database().saveTrap(trap);
        log(trap, uuid, "MEMBER_KICK", "Üye trapten çıkarıldı.");
    }

    public void role(TrapModel trap, UUID uuid, TrapRole role) {
        TrapMember member = trap.members().get(uuid);
        if (member != null) {
            member.role(role);
            plugin.database().saveTrap(trap);
            log(trap, uuid, "ROLE_CHANGE", "Rol " + role.name() + " olarak ayarlandı.");
        }
    }

    public boolean upgrade(Player player, TrapModel trap) {
        int nextLevel = trap.level() + 1;
        if (nextLevel > plugin.configManager().maxConfiguredLevel()) {
            plugin.messages().send(player, "max-level");
            return false;
        }
        double price = plugin.configManager().upgradePrice(nextLevel);
        if (!plugin.vault().withdraw(player, price)) {
            plugin.messages().send(player, "not-enough-money", MessageManager.placeholders("%price%", price));
            return false;
        }
        trap.level(nextLevel);
        trap.maxHealth(plugin.configManager().maxHealthForLevel(nextLevel));
        trap.health(trap.maxHealth());
        plugin.database().saveTrap(trap);
        log(trap, player, "UPGRADE", "Trap seviyesi " + nextLevel + " oldu.");
        plugin.holograms().update(trap);
        plugin.visuals().playUpgradeEffect(player, trap);
        plugin.messages().send(player, "upgraded", MessageManager.placeholders("%level%", nextLevel));
        return true;
    }

    public void damage(TrapModel trap, int amount) {
        trap.health(Math.max(0, trap.health() - amount));
        log(trap, (UUID) null, "DAMAGE", "Trap canı " + amount + " azaldı. Kalan: " + trap.health() + "/" + trap.maxHealth());
        plugin.visuals().playDamageEffect(trap);
        if (trap.health() <= 0) {
            UUID oldOwner = trap.owner();
            trap.owner(null);
            trap.members().clear();
            trap.trusted().clear();
            trap.forSale(false);
            trap.bankBalance(0D);
            trap.health(trap.maxHealth());
            log(trap, oldOwner, "DISBAND", "Trap canı bitti ve dağıldı.");
            if (oldOwner != null && Bukkit.getPlayer(oldOwner) != null) {
                plugin.messages().send(Bukkit.getPlayer(oldOwner), "trap-disbanded", MessageManager.placeholders("%trap%", trap.name()));
            }
        }
        plugin.database().saveTrap(trap);
        plugin.holograms().update(trap);
    }

    public void log(TrapModel trap, Player actor, String action, String detail) {
        if (actor == null) {
            log(trap, (UUID) null, action, detail);
            return;
        }
        plugin.database().addLog(trap.id(), actor.getUniqueId(), actor.getName(), action, detail);
    }

    public void log(TrapModel trap, UUID actor, String action, String detail) {
        String actorName = null;
        if (actor != null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(actor);
            actorName = offline.getName();
        }
        plugin.database().addLog(trap.id(), actor, actorName, action, detail);
    }

    public ArrayList<TrapModel> sortedTraps() {
        ArrayList<TrapModel> list = new ArrayList<>(traps.values());
        list.sort(Comparator.comparing(TrapModel::id));
        return list;
    }

    public ArrayList<TrapModel> marketTraps() {
        ArrayList<TrapModel> list = new ArrayList<>();
        for (TrapModel trap : traps.values()) {
            if (trap.forSale()) {
                list.add(trap);
            }
        }
        list.sort(Comparator.comparingDouble(TrapModel::salePrice));
        return list;
    }

    public boolean trapChat(UUID uuid) {
        return trapChat.getOrDefault(uuid, false);
    }

    public boolean toggleTrapChat(UUID uuid) {
        boolean enabled = !trapChat(uuid);
        trapChat.put(uuid, enabled);
        return enabled;
    }

    public boolean flyEnabled(UUID uuid) {
        return flyTrap.containsKey(uuid);
    }

    public void fly(UUID uuid, TrapModel trap) {
        if (trap == null) {
            flyTrap.remove(uuid);
        } else {
            flyTrap.put(uuid, trap.id());
        }
    }

    public void rebuildChunkCache() {
        chunkCache.clear();
        for (TrapModel trap : traps.values()) {
            for (TrapChunk chunk : trap.chunks()) {
                chunkCache.put(chunk.key(), trap);
            }
        }
    }
}
