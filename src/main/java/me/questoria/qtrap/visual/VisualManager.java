package me.questoria.qtrap.visual;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.model.TrapChunk;
import me.questoria.qtrap.model.TrapModel;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class VisualManager {
    private final QTrapPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, String> lastTrap = new HashMap<>();
    private final Set<UUID> boundaryViewers = new HashSet<>();
    private BukkitTask task;

    public VisualManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        long interval = Math.max(5L, plugin.getConfig().getLong("visuals.update-interval-ticks", 20L));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
        lastTrap.clear();
        boundaryViewers.clear();
    }

    public boolean toggleBoundary(Player player) {
        UUID uuid = player.getUniqueId();
        if (!boundaryViewers.add(uuid)) {
            boundaryViewers.remove(uuid);
            return false;
        }
        return true;
    }

    public void playBuyEffect(Player player, TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.effects.buy.enabled", true)) {
            return;
        }
        Location location = effectLocation(player, trap);
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 40, 1.2D, 0.8D, 1.2D, 0.02D);
        location.getWorld().spawnParticle(Particle.DUST, location, 35, 1.0D, 0.5D, 1.0D, 0, new Particle.DustOptions(Color.fromRGB(60, 220, 80), 1.5F));
        playSound(player, "visuals.effects.buy.sound", Sound.ENTITY_PLAYER_LEVELUP);
    }

    public void playUpgradeEffect(Player player, TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.effects.upgrade.enabled", true)) {
            return;
        }
        Location location = effectLocation(player, trap);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 55, 1.4D, 1.0D, 1.4D, 0.04D);
        location.getWorld().spawnParticle(Particle.DUST, location, 40, 1.2D, 0.6D, 1.2D, 0, new Particle.DustOptions(Color.fromRGB(220, 35, 35), 1.6F));
        playSound(player, "visuals.effects.upgrade.sound", Sound.UI_TOAST_CHALLENGE_COMPLETE);
    }

    public void playDamageEffect(TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.effects.damage.enabled", true) || trap.spawn() == null || trap.spawn().getWorld() == null) {
            return;
        }
        Location location = trap.spawn().clone().add(0, 1.2D, 0);
        location.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, location, 20, 1.4D, 0.8D, 1.4D, 0.02D);
        location.getWorld().spawnParticle(Particle.DUST, location, 30, 1.1D, 0.5D, 1.1D, 0, new Particle.DustOptions(Color.fromRGB(255, 30, 30), 1.5F));
    }

    public void playDeniedEffect(Player player) {
        if (!plugin.getConfig().getBoolean("visuals.effects.denied.enabled", true)) {
            return;
        }
        Location location = player.getLocation().add(0, 1.0D, 0);
        player.spawnParticle(Particle.SMOKE, location, 12, 0.35D, 0.35D, 0.35D, 0.01D);
        player.spawnParticle(Particle.BLOCK_MARKER, location, 1, Material.BARRIER.createBlockData());
        playSound(player, "visuals.effects.denied.sound", Sound.BLOCK_NOTE_BLOCK_BASS);
    }

    private void tick() {
        bossBars.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        lastTrap.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        boundaryViewers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            TrapModel trap = plugin.traps().at(player.getLocation()).orElse(null);
            updateTitles(player, trap);
            updateActionBar(player, trap);
            updateBossBar(player, trap);
            if (boundaryViewers.contains(player.getUniqueId())) {
                if (trap == null) {
                    plugin.messages().send(player, "boundary-disabled");
                    boundaryViewers.remove(player.getUniqueId());
                } else {
                    drawBoundary(player, trap);
                }
            }
        }
    }

    private void updateTitles(Player player, TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.title.enabled", true)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        String current = trap == null ? null : trap.id();
        String previous = lastTrap.get(uuid);
        if (current != null && !current.equals(previous)) {
            player.sendTitle(
                    MessageManager.color(apply(plugin.getConfig().getString("visuals.title.enter-title", "&c%name%"), trap)),
                    MessageManager.color(apply(plugin.getConfig().getString("visuals.title.enter-subtitle", "&7Sahip: &f%owner%"), trap)),
                    5, 35, 10
            );
        } else if (current == null && previous != null) {
            player.sendTitle(
                    MessageManager.color(plugin.getConfig().getString("visuals.title.exit-title", "&cTrap Bölgesinden Çıkıldı")),
                    MessageManager.color(plugin.getConfig().getString("visuals.title.exit-subtitle", "&7Artık trap alanında değilsin.")),
                    5, 25, 10
            );
        }
        if (current == null) {
            lastTrap.remove(uuid);
        } else {
            lastTrap.put(uuid, current);
        }
    }

    private void updateActionBar(Player player, TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.actionbar.enabled", true) || trap == null) {
            return;
        }
        String line = apply(plugin.getConfig().getString("visuals.actionbar.format",
                "&cQTrap &8| &fTrap: &c%id% &8| &fSahip: &c%owner% &8| &fCan: %health_color%%health%&7/&c%max_health%"), trap);
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(MessageManager.color(line)));
    }

    private void updateBossBar(Player player, TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.bossbar.enabled", true) || trap == null) {
            removeBossBar(player);
            return;
        }
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), uuid -> {
            BossBar created = Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_10);
            created.addPlayer(player);
            return created;
        });
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        double progress = trap.maxHealth() <= 0 ? 0D : trap.health() / (double) trap.maxHealth();
        bar.setTitle(MessageManager.color(apply(plugin.getConfig().getString("visuals.bossbar.format",
                "&cQTrap &8| &f%name% &8- %health_color%%health%&7/&c%max_health%"), trap)));
        bar.setProgress(Math.max(0.0D, Math.min(1.0D, progress)));
        bar.setColor(bossColor(progress));
    }

    private void removeBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void drawBoundary(Player player, TrapModel trap) {
        if (!plugin.getConfig().getBoolean("visuals.boundary.enabled", true)) {
            return;
        }
        World world = player.getWorld();
        Particle.DustOptions dust = new Particle.DustOptions(boundaryColor(player, trap), 1.3F);
        double y = player.getLocation().getY() + plugin.getConfig().getDouble("visuals.boundary.y-offset", 0.15D);
        int step = Math.max(1, plugin.getConfig().getInt("visuals.boundary.step", 2));
        for (TrapChunk chunk : trap.chunks()) {
            if (!chunk.world().equals(world.getName())) {
                continue;
            }
            int minX = chunk.x() * 16;
            int maxX = minX + 16;
            int minZ = chunk.z() * 16;
            int maxZ = minZ + 16;
            for (int x = minX; x <= maxX; x += step) {
                spawnDust(player, world, x, y, minZ, dust);
                spawnDust(player, world, x, y, maxZ, dust);
            }
            for (int z = minZ; z <= maxZ; z += step) {
                spawnDust(player, world, minX, y, z, dust);
                spawnDust(player, world, maxX, y, z, dust);
            }
        }
    }

    private void spawnDust(Player player, World world, double x, double y, double z, Particle.DustOptions dust) {
        player.spawnParticle(Particle.DUST, new Location(world, x + 0.5D, y, z + 0.5D), 1, 0, 0, 0, 0, dust);
    }

    private Location effectLocation(Player player, TrapModel trap) {
        if (trap != null && trap.spawn() != null && trap.spawn().getWorld() != null) {
            return trap.spawn().clone().add(0, 1.1D, 0);
        }
        return player.getLocation().add(0, 1.1D, 0);
    }

    private void playSound(Player player, String path, Sound fallback) {
        String raw = plugin.getConfig().getString(path, fallback.name());
        try {
            player.playSound(player.getLocation(), Sound.valueOf(raw), 1.0F, 1.0F);
        } catch (IllegalArgumentException exception) {
            player.playSound(player.getLocation(), fallback, 1.0F, 1.0F);
        }
    }

    private BarColor bossColor(double progress) {
        if (!plugin.getConfig().getBoolean("visuals.bossbar.dynamic-color", true)) {
            return BarColor.RED;
        }
        if (progress >= 0.66D) {
            return BarColor.GREEN;
        }
        if (progress >= 0.33D) {
            return BarColor.YELLOW;
        }
        return BarColor.RED;
    }

    private Color boundaryColor(Player player, TrapModel trap) {
        if (trap.forSale()) {
            return Color.fromRGB(255, 210, 40);
        }
        if (!trap.owned()) {
            return Color.fromRGB(80, 230, 100);
        }
        if (trap.isMember(player.getUniqueId())) {
            return Color.fromRGB(220, 35, 35);
        }
        return Color.fromRGB(130, 130, 130);
    }

    private String apply(String text, TrapModel trap) {
        return MessageManager.apply(text, MessageManager.placeholders(
                "%id%", trap.id(),
                "%name%", trap.name(),
                "%owner%", ownerName(trap),
                "%level%", trap.level(),
                "%health%", trap.health(),
                "%max_health%", trap.maxHealth(),
                "%bank%", plugin.vault().format(trap.bankBalance()),
                "%status%", status(trap),
                "%status_color%", statusColor(trap),
                "%health_color%", healthColor(trap),
                "%level_color%", levelColor(trap)
        ));
    }

    private String status(TrapModel trap) {
        if (trap.forSale()) {
            return "Satılık";
        }
        return trap.owned() ? "Sahipli" : "Satın Alınabilir";
    }

    private String statusColor(TrapModel trap) {
        if (trap.forSale()) {
            return "&e";
        }
        return trap.owned() ? "&c" : "&a";
    }

    private String healthColor(TrapModel trap) {
        double progress = trap.maxHealth() <= 0 ? 0D : trap.health() / (double) trap.maxHealth();
        if (progress >= 0.66D) {
            return "&a";
        }
        if (progress >= 0.33D) {
            return "&e";
        }
        return "&c";
    }

    private String levelColor(TrapModel trap) {
        if (trap.level() >= 4) {
            return "&d";
        }
        if (trap.level() >= 3) {
            return "&b";
        }
        if (trap.level() >= 2) {
            return "&e";
        }
        return "&f";
    }

    private String ownerName(TrapModel trap) {
        if (trap.owner() == null) {
            return "Yok";
        }
        String name = Bukkit.getOfflinePlayer(trap.owner()).getName();
        return name == null ? trap.owner().toString().substring(0, 8) : name;
    }
}
