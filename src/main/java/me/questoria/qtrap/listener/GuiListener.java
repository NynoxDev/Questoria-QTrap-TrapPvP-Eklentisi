package me.questoria.qtrap.listener;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.gui.GuiManager;
import me.questoria.qtrap.gui.GuiType;
import me.questoria.qtrap.gui.QTrapHolder;
import me.questoria.qtrap.model.TrapModel;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class GuiListener implements Listener {
    private final QTrapPlugin plugin;

    public GuiListener(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof QTrapHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        playClick(player);

        int slot = event.getRawSlot();
        TrapModel trap = holder.trapId() == null ? null : plugin.traps().byId(holder.trapId()).orElse(null);

        if (holder.type() == GuiType.MAIN) {
            handleMain(player, slot);
            return;
        }
        if (holder.type() == GuiType.LIST || holder.type() == GuiType.MARKET) {
            handleList(player, holder, slot);
            return;
        }
        if (holder.type() == GuiType.LOGS) {
            if (slot == 49) {
                plugin.gui().openMain(player);
            }
            return;
        }
        if (holder.type() == GuiType.MEMBERS && trap != null) {
            if (slot == 45) {
                plugin.gui().openMembers(player, trap, holder.page() - 1);
            } else if (slot == 53) {
                plugin.gui().openMembers(player, trap, holder.page() + 1);
            } else if (slot >= 0 && slot <= 44) {
                plugin.gui().openRoles(player, trap);
            }
            return;
        }
        if (holder.type() == GuiType.ROLES && trap != null && slot == 15) {
            plugin.gui().openMembers(player, trap, 1);
            return;
        }
        if (holder.type() == GuiType.CONFIRM_BUY && trap != null) {
            if (slot == 11 && plugin.traps().buy(player, trap, trap.salePrice())) {
                plugin.messages().send(player, "bought", MessageManager.placeholders("%trap%", trap.name()));
                player.closeInventory();
            } else if (slot == 15) {
                player.closeInventory();
            }
            return;
        }
        if (holder.type() == GuiType.UPGRADE && trap != null && slot >= 12 && slot <= 15) {
            plugin.traps().upgrade(player, trap);
            player.closeInventory();
        }
    }

    private void playClick(Player player) {
        String raw = plugin.getConfig().getString("sounds.click", "UI_BUTTON_CLICK");
        try {
            player.playSound(player.getLocation(), Sound.valueOf(raw), 0.7F, 1.1F);
        } catch (IllegalArgumentException ignored) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7F, 1.1F);
        }
    }

    private void handleMain(Player player, int slot) {
        TrapModel own = plugin.traps().ownedBy(player.getUniqueId()).orElse(null);
        TrapModel current = plugin.traps().at(player.getLocation()).orElse(null);

        switch (slot) {
            case 4, 11, 19 -> {
                if (current != null) plugin.gui().openInfo(player, current);
            }
            case 10 -> player.performCommand("trap al");
            case 12, 30 -> plugin.gui().openList(player, 1, false);
            case 13, 31 -> plugin.gui().openList(player, 1, true);
            case 14, 21 -> player.performCommand("trap ziyaret");
            case 15 -> player.performCommand("trap fly");
            case 20 -> {
                if (own != null) plugin.gui().openMembers(player, own, 1);
            }
            case 22 -> player.performCommand("trap spawn");
            case 23 -> player.performCommand("trap setspawn");
            case 24 -> {
                if (own != null) plugin.gui().openBank(player, own);
            }
            case 25 -> player.performCommand("trap pvp");
            case 29 -> {
                if (own != null) plugin.gui().openUpgrade(player, own);
            }
            case 32 -> player.performCommand("trap satışiptal");
            case 33 -> player.performCommand("trap sohbet");
            case 34 -> player.performCommand("trap log");
            case 40 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleList(Player player, QTrapHolder holder, int slot) {
        boolean market = holder.type() == GuiType.MARKET;
        if (slot == 45) {
            plugin.gui().openList(player, holder.page() - 1, market, holder.mode());
            return;
        }
        if (slot == 53) {
            plugin.gui().openList(player, holder.page() + 1, market, holder.mode());
            return;
        }
        if (market && slot == 46) {
            plugin.gui().openList(player, 1, true, 0);
            return;
        }
        if (market && slot == 47) {
            plugin.gui().openList(player, 1, true, 1);
            return;
        }
        if (market && slot == 51) {
            plugin.gui().openList(player, 1, true, 2);
            return;
        }

        List<TrapModel> traps = market ? plugin.traps().marketTraps() : plugin.traps().sortedTraps();
        if (market) {
            traps = new java.util.ArrayList<>(traps);
            switch (holder.mode()) {
                case 1 -> traps.sort(java.util.Comparator.<TrapModel>comparingInt(trap -> trap.level()).reversed());
                case 2 -> traps.sort(java.util.Comparator.<TrapModel>comparingInt(trap -> trap.health()).reversed());
                default -> traps.sort(java.util.Comparator.comparingDouble(trap -> trap.salePrice()));
            }
        }
        int index = -1;
        for (int i = 0; i < GuiManager.LIST_SLOTS.length; i++) {
            if (GuiManager.LIST_SLOTS[i] == slot) {
                index = i;
                break;
            }
        }
        if (index < 0) return;

        int absolute = (holder.page() - 1) * GuiManager.LIST_SLOTS.length + index;
        if (absolute >= traps.size()) return;
        TrapModel trap = traps.get(absolute);
        if (!trap.owned() || trap.forSale()) {
            plugin.gui().openConfirmBuy(player, trap);
        } else {
            plugin.gui().openInfo(player, trap);
        }
    }
}
