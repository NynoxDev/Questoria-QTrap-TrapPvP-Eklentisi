package me.questoria.qtrap.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TrapModel {
    private final String id;
    private String name;
    private UUID owner;
    private int level;
    private int health;
    private int maxHealth;
    private double bankBalance;
    private boolean forSale;
    private double salePrice;
    private boolean pvp;
    private boolean visit;
    private Location spawn;
    private long createdAt;
    private long updatedAt;
    private final Set<TrapChunk> chunks = new LinkedHashSet<>();
    private final Map<UUID, TrapMember> members = new LinkedHashMap<>();
    private final Map<UUID, Set<String>> trusted = new LinkedHashMap<>();

    public TrapModel(String id) {
        this.id = id;
        this.name = "Trap " + id;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public String id() { return id; }
    public String name() { return name; }
    public void name(String name) { this.name = name; touch(); }
    public UUID owner() { return owner; }
    public void owner(UUID owner) { this.owner = owner; touch(); }
    public int level() { return level; }
    public void level(int level) { this.level = level; touch(); }
    public int health() { return health; }
    public void health(int health) { this.health = health; touch(); }
    public int maxHealth() { return maxHealth; }
    public void maxHealth(int maxHealth) { this.maxHealth = maxHealth; touch(); }
    public double bankBalance() { return bankBalance; }
    public void bankBalance(double bankBalance) { this.bankBalance = bankBalance; touch(); }
    public boolean forSale() { return forSale; }
    public void forSale(boolean forSale) { this.forSale = forSale; touch(); }
    public double salePrice() { return salePrice; }
    public void salePrice(double salePrice) { this.salePrice = salePrice; touch(); }
    public boolean pvp() { return pvp; }
    public void pvp(boolean pvp) { this.pvp = pvp; touch(); }
    public boolean visit() { return visit; }
    public void visit(boolean visit) { this.visit = visit; touch(); }
    public Location spawn() { return spawn; }
    public void spawn(Location spawn) { this.spawn = spawn; touch(); }
    public long createdAt() { return createdAt; }
    public void createdAt(long createdAt) { this.createdAt = createdAt; }
    public long updatedAt() { return updatedAt; }
    public void updatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public Set<TrapChunk> chunks() { return chunks; }
    public Map<UUID, TrapMember> members() { return members; }
    public Map<UUID, Set<String>> trusted() { return trusted; }

    public boolean owned() {
        return owner != null;
    }

    public boolean isMember(UUID uuid) {
        return owner != null && owner.equals(uuid) || members.containsKey(uuid);
    }

    public TrapRole role(UUID uuid) {
        if (owner != null && owner.equals(uuid)) {
            return TrapRole.OWNER;
        }
        TrapMember member = members.get(uuid);
        return member == null ? null : member.role();
    }

    public Collection<TrapMember> memberValues() {
        return members.values();
    }

    public void touch() {
        updatedAt = System.currentTimeMillis();
    }

    public static String serializeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public static Location deserializeLocation(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String[] split = raw.split(",");
            if (split.length < 4 || Bukkit.getWorld(split[0]) == null) {
                return null;
            }
            float yaw = split.length > 4 ? Float.parseFloat(split[4]) : 0F;
            float pitch = split.length > 5 ? Float.parseFloat(split[5]) : 0F;
            return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), yaw, pitch);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
