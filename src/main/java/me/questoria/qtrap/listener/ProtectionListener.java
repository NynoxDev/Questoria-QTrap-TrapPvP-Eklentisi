package me.questoria.qtrap.listener;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.model.TrapModel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class ProtectionListener implements Listener {
    private final QTrapPlugin plugin;

    public ProtectionListener(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("protection.protect-block-break", true)) return;
        TrapModel trap = plugin.traps().at(event.getBlock().getLocation()).orElse(null);
        if (trap == null || can(event.getPlayer(), trap, "break")) return;
        event.setCancelled(true);
        plugin.messages().send(event.getPlayer(), "protected");
        if (plugin.getConfig().getBoolean("protection.damage-health-on-denied-break", true)) {
            int damage = plugin.getConfig().getInt("protection.denied-break-damage", 1);
            plugin.traps().damage(trap, damage);
            plugin.messages().send(event.getPlayer(), "health-damaged", MessageManager.placeholders("%health%", trap.health(), "%max_health%", trap.maxHealth()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!plugin.getConfig().getBoolean("protection.protect-block-place", true)) return;
        TrapModel trap = plugin.traps().at(event.getBlock().getLocation()).orElse(null);
        if (trap == null || can(event.getPlayer(), trap, "place")) return;
        event.setCancelled(true);
        plugin.messages().send(event.getPlayer(), "protected");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("protection.protect-interact", true)) return;
        if (event.getClickedBlock() == null) return;
        TrapModel trap = plugin.traps().at(event.getClickedBlock().getLocation()).orElse(null);
        if (trap == null || can(event.getPlayer(), trap, "interact")) return;
        Material type = event.getClickedBlock().getType();
        if (type.isInteractable()) {
            event.setCancelled(true);
            plugin.messages().send(event.getPlayer(), "protected");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.getConfig().getBoolean("protection.protect-containers", true)) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Container container)) return;
        TrapModel trap = plugin.traps().at(container.getLocation()).orElse(null);
        if (trap == null || can(player, trap, "chest")) return;
        event.setCancelled(true);
        plugin.messages().send(player, "protected");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        TrapModel trap = plugin.traps().at(victim.getLocation()).orElse(null);
        if (trap == null || trap.pvp()) return;
        event.setCancelled(true);
        plugin.messages().send(attacker, "protected");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;
        Player player = event.getPlayer();
        TrapModel trap = plugin.traps().at(player.getLocation()).orElse(null);
        if (plugin.traps().flyEnabled(player.getUniqueId()) && (trap == null || !trap.isMember(player.getUniqueId()))) {
            plugin.traps().fly(player.getUniqueId(), null);
            player.setFlying(false);
            player.setAllowFlight(false);
            plugin.messages().send(player, "fly-disabled");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.traps().trapChat(event.getPlayer().getUniqueId())) return;
        TrapModel trap = plugin.traps().memberTrap(event.getPlayer().getUniqueId()).orElse(null);
        if (trap == null) return;
        event.setCancelled(true);
        String message = MessageManager.color("&8[&cTrap&8] &f" + event.getPlayer().getName() + " &8> &7" + event.getMessage());
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (trap.isMember(online.getUniqueId())) {
                    online.sendMessage(message);
                }
            }
        });
    }

    private boolean can(Player player, TrapModel trap, String permission) {
        return player.hasPermission("qtrap.bypass") || trap.isMember(player.getUniqueId()) && plugin.traps().hasPermission(trap, player.getUniqueId(), permission);
    }
}
