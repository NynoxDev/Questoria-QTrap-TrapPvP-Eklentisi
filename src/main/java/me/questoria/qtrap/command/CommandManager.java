package me.questoria.qtrap.command;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.model.TrapChunk;
import me.questoria.qtrap.model.TrapModel;
import me.questoria.qtrap.model.TrapRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CommandManager implements CommandExecutor, TabCompleter {
    private final QTrapPlugin plugin;

    public CommandManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("qtrap")) {
                return admin(sender, args);
            }
            if (command.getName().equalsIgnoreCase("trapachunk")) {
                return trapAChunk(sender, args);
            }
            return trap(sender, args);
        } catch (NumberFormatException exception) {
            plugin.messages().send(sender, "invalid-number");
        } catch (IllegalArgumentException exception) {
            plugin.messages().send(sender, "invalid-value");
        }
        return true;
    }

    private boolean trapAChunk(CommandSender sender, String[] args) {
        if (!sender.hasPermission("qtrap.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (!(sender instanceof Player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (args.length != 1) {
            plugin.messages().send(sender, "admin-addchunk-usage");
            return true;
        }
        String[] forwarded = new String[]{"addchunk", args[0]};
        return admin(sender, forwarded);
    }

    private boolean trap(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            plugin.gui().openMain(player);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        TrapModel current = plugin.traps().at(player.getLocation()).orElse(null);
        TrapModel own = plugin.traps().ownedBy(player.getUniqueId()).orElse(null);
        switch (sub) {
            case "al" -> {
                if (current == null) {
                    plugin.messages().send(player, "not-in-trap");
                    return true;
                }
                plugin.gui().openConfirmBuy(player, current);
            }
            case "bilgi" -> {
                if (current == null) {
                    plugin.messages().send(player, "not-in-trap");
                    return true;
                }
                plugin.gui().openInfo(player, current);
            }
            case "liste" -> plugin.gui().openList(player, 1, false);
            case "pazar" -> plugin.gui().openList(player, 1, true);
            case "sınır", "sinir" -> {
                boolean enabled = plugin.visuals().toggleBoundary(player);
                plugin.messages().send(player, enabled ? "boundary-enabled" : "boundary-disabled");
            }
            case "log", "kayıt", "kayit" -> {
                TrapModel target = own != null ? own : current;
                if (target == null || !target.isMember(player.getUniqueId())) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                plugin.database().loadLogs(target.id(), plugin.getConfig().getInt("activity-logs.gui-limit", 28))
                        .thenAccept(logs -> Bukkit.getScheduler().runTask(plugin, () -> plugin.gui().openLogs(player, target, logs)));
            }
            case "davet" -> {
                if (own == null || !plugin.traps().hasPermission(own, player.getUniqueId(), "invite")) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                if (args.length < 2 || Bukkit.getPlayerExact(args[1]) == null) {
                    plugin.messages().send(player, "trap-not-found");
                    return true;
                }
                plugin.traps().invite(player, Bukkit.getPlayerExact(args[1]), own);
            }
            case "kabul" -> plugin.messages().send(player, plugin.traps().acceptInvite(player) ? "invite-accepted" : "trap-not-found");
            case "reddet" -> plugin.messages().send(player, plugin.traps().denyInvite(player) ? "invite-denied" : "trap-not-found");
            case "at" -> {
                if (own == null || args.length < 2 || !plugin.traps().hasPermission(own, player.getUniqueId(), "kick")) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.traps().kick(own, target.getUniqueId());
                plugin.messages().send(player, "member-kicked", MessageManager.placeholders("%player%", args[1]));
            }
            case "rol" -> {
                if (own == null || args.length < 3 || !plugin.traps().hasPermission(own, player.getUniqueId(), "role")) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                TrapRole role = parseRole(args[2]);
                if (role == TrapRole.OWNER) {
                    plugin.messages().send(player, "no-permission");
                    return true;
                }
                plugin.traps().role(own, target.getUniqueId(), role);
                plugin.messages().send(player, "role-changed", MessageManager.placeholders("%player%", args[1], "%role%", role.name()));
            }
            case "banka" -> {
                if (own == null) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                plugin.gui().openBank(player, own);
            }
            case "para" -> money(player, args, own);
            case "yükselt", "yukselt" -> {
                if (own == null || !plugin.traps().hasPermission(own, player.getUniqueId(), "upgrade")) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                plugin.gui().openUpgrade(player, own);
            }
            case "satış", "satis" -> sale(player, args, own);
            case "satışiptal", "satisiptal" -> {
                if (own == null) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                own.forSale(false);
                plugin.database().saveTrap(own);
                plugin.traps().log(own, player, "SALE_CANCEL", "Trap satışı iptal edildi.");
                plugin.holograms().update(own);
                plugin.messages().send(player, "sale-cancelled");
            }
            case "spawn" -> {
                TrapModel target = own != null ? own : current;
                if (target == null || target.spawn() == null) {
                    plugin.messages().send(player, "not-in-trap");
                    return true;
                }
                player.teleport(target.spawn());
                plugin.messages().send(player, "teleported");
            }
            case "setspawn" -> {
                if (own == null || !plugin.traps().hasPermission(own, player.getUniqueId(), "setspawn")) {
                    plugin.messages().send(player, "not-owner");
                    return true;
                }
                own.spawn(player.getLocation());
                plugin.database().saveTrap(own);
                plugin.traps().log(own, player, "SPAWN_SET", "Trap spawn noktası güncellendi.");
                plugin.holograms().update(own);
                plugin.messages().send(player, "spawn-set");
            }
            case "ziyaret" -> {
                if (args.length >= 2) {
                    TrapModel target = plugin.traps().byId(args[1]).orElse(null);
                    if (target != null && target.visit() && target.spawn() != null) {
                        player.teleport(target.spawn());
                        plugin.messages().send(player, "teleported");
                    }
                    return true;
                }
                toggleVisit(player, own);
            }
            case "pvp" -> togglePvp(player, own);
            case "fly" -> toggleFly(player, current);
            case "sohbet" -> {
                boolean enabled = plugin.traps().toggleTrapChat(player.getUniqueId());
                plugin.messages().send(player, enabled ? "chat-enabled" : "chat-disabled");
            }
            default -> plugin.gui().openMain(player);
        }
        return true;
    }

    private void money(Player player, String[] args, TrapModel trap) {
        if (trap == null || args.length < 3) {
            plugin.messages().send(player, "not-owner");
            return;
        }
        double amount = Double.parseDouble(args[2]);
        if (amount <= 0) {
            plugin.messages().send(player, "invalid-number");
            return;
        }
        if (args[1].equalsIgnoreCase("yatır") || args[1].equalsIgnoreCase("yatir")) {
            if (!plugin.traps().hasPermission(trap, player.getUniqueId(), "bank-deposit")) {
                plugin.messages().send(player, "no-permission");
                return;
            }
            if (trap.bankBalance() + amount > plugin.configManager().bankLimit(trap.level())) {
                plugin.messages().send(player, "bank-limit");
                return;
            }
            if (plugin.vault().withdraw(player, amount)) {
                trap.bankBalance(trap.bankBalance() + amount);
                plugin.database().saveTrap(trap);
                plugin.traps().log(trap, player, "BANK_DEPOSIT", plugin.vault().format(amount) + " yatırıldı.");
                plugin.holograms().update(trap);
                plugin.messages().send(player, "bank-deposit", MessageManager.placeholders("%amount%", plugin.vault().format(amount)));
            }
        } else if (args[1].equalsIgnoreCase("çek") || args[1].equalsIgnoreCase("cek")) {
            if (!plugin.traps().hasPermission(trap, player.getUniqueId(), "bank-withdraw")) {
                plugin.messages().send(player, "no-permission");
                return;
            }
            amount = Math.min(amount, trap.bankBalance());
            trap.bankBalance(trap.bankBalance() - amount);
            plugin.vault().deposit(player, amount);
            plugin.database().saveTrap(trap);
            plugin.traps().log(trap, player, "BANK_WITHDRAW", plugin.vault().format(amount) + " çekildi.");
            plugin.holograms().update(trap);
            plugin.messages().send(player, "bank-withdraw", MessageManager.placeholders("%amount%", plugin.vault().format(amount)));
        }
    }

    private void sale(Player player, String[] args, TrapModel trap) {
        if (trap == null || args.length < 2 || !plugin.traps().hasPermission(trap, player.getUniqueId(), "sell")) {
            plugin.messages().send(player, "not-owner");
            return;
        }
        double price = Double.parseDouble(args[1]);
        if (price < plugin.getConfig().getDouble("market.min-price", 1D) || price > plugin.getConfig().getDouble("market.max-price", 1000000000D)) {
            plugin.messages().send(player, "invalid-number");
            return;
        }
        trap.forSale(true);
        trap.salePrice(price);
        plugin.database().saveTrap(trap);
        plugin.traps().log(trap, player, "SALE", "Trap satışa çıkarıldı. Fiyat: " + plugin.vault().format(price));
        plugin.holograms().update(trap);
        plugin.messages().send(player, "sold", MessageManager.placeholders("%price%", plugin.vault().format(price)));
    }

    private void togglePvp(Player player, TrapModel trap) {
        if (trap == null || !plugin.traps().hasPermission(trap, player.getUniqueId(), "pvp")) {
            plugin.messages().send(player, "not-owner");
            return;
        }
        trap.pvp(!trap.pvp());
        plugin.database().saveTrap(trap);
        plugin.traps().log(trap, player, "PVP_TOGGLE", "PvP durumu " + (trap.pvp() ? "Açık" : "Kapalı") + " yapıldı.");
        plugin.holograms().update(trap);
        plugin.messages().send(player, "pvp-toggle", MessageManager.placeholders("%state%", trap.pvp() ? "Açık" : "Kapalı"));
    }

    private void toggleVisit(Player player, TrapModel trap) {
        if (trap == null || !plugin.traps().hasPermission(trap, player.getUniqueId(), "visit")) {
            plugin.messages().send(player, "not-owner");
            return;
        }
        trap.visit(!trap.visit());
        plugin.database().saveTrap(trap);
        plugin.traps().log(trap, player, "VISIT_TOGGLE", "Ziyaret durumu " + (trap.visit() ? "Açık" : "Kapalı") + " yapıldı.");
        plugin.holograms().update(trap);
        plugin.messages().send(player, "visit-toggle", MessageManager.placeholders("%state%", trap.visit() ? "Açık" : "Kapalı"));
    }

    private void toggleFly(Player player, TrapModel current) {
        if (current == null || !current.isMember(player.getUniqueId()) || !plugin.traps().hasPermission(current, player.getUniqueId(), "fly")) {
            plugin.messages().send(player, "not-in-trap");
            return;
        }
        if (plugin.traps().flyEnabled(player.getUniqueId())) {
            plugin.traps().fly(player.getUniqueId(), null);
            player.setAllowFlight(false);
            player.setFlying(false);
            plugin.messages().send(player, "fly-disabled");
        } else {
            plugin.traps().fly(player.getUniqueId(), current);
            player.setAllowFlight(true);
            plugin.messages().send(player, "fly-enabled");
        }
    }

    private boolean admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("qtrap.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length == 0) {
            plugin.messages().send(sender, "admin-help");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            plugin.reloadPlugin();
            plugin.messages().send(sender, "reloaded");
            return true;
        }
        if (!(sender instanceof Player player) && Arrays.asList("create", "addchunk", "removechunk").contains(sub)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        try {
            switch (sub) {
                case "create" -> {
                    if (args.length < 3) {
                        plugin.messages().send(sender, "admin-create-usage");
                        return true;
                    }
                    Player player = (Player) sender;
                    if (plugin.traps().byId(args[1]).isPresent()) {
                        plugin.messages().send(sender, "trap-already-exists");
                        return true;
                    }
                    if (plugin.traps().at(player.getLocation()).isPresent()) {
                        plugin.messages().send(sender, "chunk-already-used");
                        return true;
                    }
                    double price = Double.parseDouble(args[2]);
                    if (price < 0D) {
                        plugin.messages().send(sender, "invalid-number");
                        return true;
                    }
                    TrapModel trap = plugin.traps().create(args[1], price, player.getLocation());
                    plugin.messages().send(sender, "created", MessageManager.placeholders("%id%", trap.id()));
                }
                case "delete" -> {
                    if (!requireId(sender, sub, args)) return true;
                    TrapModel trap = plugin.traps().byId(args[1]).orElse(null);
                    if (trap == null) {
                        plugin.messages().send(sender, "trap-not-found");
                        return true;
                    }
                    plugin.traps().delete(trap);
                    plugin.messages().send(sender, "deleted", MessageManager.placeholders("%id%", trap.id()));
                }
                case "setprice" -> {
                    if (!requireValue(sender, sub, args)) return true;
                    double price = Double.parseDouble(args[2]);
                    if (price < 0D) {
                        plugin.messages().send(sender, "invalid-number");
                        return true;
                    }
                    editOrMessage(sender, args[1], trap -> trap.salePrice(price));
                }
                case "setowner" -> {
                    if (!requireValue(sender, sub, args)) return true;
                    editOrMessage(sender, args[1], trap -> trap.owner(Bukkit.getOfflinePlayer(args[2]).getUniqueId()));
                }
                case "addchunk" -> {
                    if (!requireId(sender, sub, args)) return true;
                    addChunk(sender, args);
                }
                case "removechunk" -> {
                    if (!requireId(sender, sub, args)) return true;
                    removeChunk(sender, args);
                }
                case "sethealth" -> {
                    if (!requireValue(sender, sub, args)) return true;
                    int health = Integer.parseInt(args[2]);
                    if (health < 0) {
                        plugin.messages().send(sender, "invalid-number");
                        return true;
                    }
                    editOrMessage(sender, args[1], trap -> trap.health(health));
                }
                case "setmaxhealth" -> {
                    if (!requireValue(sender, sub, args)) return true;
                    int maxHealth = Integer.parseInt(args[2]);
                    if (maxHealth <= 0) {
                        plugin.messages().send(sender, "invalid-number");
                        return true;
                    }
                    editOrMessage(sender, args[1], trap -> {
                        trap.maxHealth(maxHealth);
                        trap.health(Math.min(trap.health(), maxHealth));
                    });
                }
                case "setlevel" -> {
                    if (!requireValue(sender, sub, args)) return true;
                    int level = Integer.parseInt(args[2]);
                    if (level <= 0 || level > plugin.configManager().maxConfiguredLevel()) {
                        plugin.messages().send(sender, "invalid-number");
                        return true;
                    }
                    editOrMessage(sender, args[1], trap -> trap.level(level));
                }
                case "info" -> {
                    if (!requireId(sender, sub, args)) return true;
                    TrapModel trap = plugin.traps().byId(args[1]).orElse(null);
                    if (trap == null) {
                        plugin.messages().send(sender, "trap-not-found");
                        return true;
                    }
                    sender.sendMessage(MessageManager.color("&8[&cQTrap&8] &fTrap &c" + trap.id()
                            + " &7| Sahip: &f" + trap.owner()
                            + " &7| Seviye: &f" + trap.level()
                            + " &7| Can: &f" + trap.health() + "/" + trap.maxHealth()
                            + " &7| Chunk: &f" + trap.chunks().size()));
                }
                default -> plugin.messages().send(sender, "admin-usage");
            }
        } catch (NumberFormatException exception) {
            plugin.messages().send(sender, "invalid-number");
        }
        return true;
    }

    private TrapRole parseRole(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "yonetici", "manager" -> TrapRole.MANAGER;
            case "moderator", "mod" -> TrapRole.MODERATOR;
            case "uye", "member" -> TrapRole.MEMBER;
            case "owner", "sahip" -> TrapRole.OWNER;
            default -> TrapRole.valueOf(raw.toUpperCase(Locale.ROOT));
        };
    }

    private void addChunk(CommandSender sender, String[] args) {
        TrapModel trap = plugin.traps().byId(args[1]).orElse(null);
        if (trap == null) {
            plugin.messages().send(sender, "trap-not-found");
            return;
        }
        TrapChunk chunk = commandChunk((Player) sender, args);
        if (chunk == null) {
                    plugin.messages().send(sender, "admin-addchunk-usage");
            return;
        }
        TrapModel existing = plugin.traps().at(chunk).orElse(null);
        if (existing != null) {
            plugin.messages().send(sender, existing.id().equalsIgnoreCase(trap.id()) ? "chunk-already-in-trap" : "chunk-already-used");
            return;
        }
        if (!trap.chunks().add(chunk)) {
            plugin.messages().send(sender, "chunk-already-in-trap");
            return;
        }
        plugin.traps().rebuildChunkCache();
        plugin.database().saveTrap(trap);
        plugin.traps().log(trap, (Player) sender, "CHUNK_ADD", "Chunk eklendi: " + chunk.x() + ", " + chunk.z());
        plugin.holograms().update(trap);
        plugin.messages().send(sender, "chunk-added", MessageManager.placeholders("%id%", trap.id(), "%chunks%", trap.chunks().size()));
    }

    private void removeChunk(CommandSender sender, String[] args) {
        TrapModel trap = plugin.traps().byId(args[1]).orElse(null);
        if (trap == null) {
            plugin.messages().send(sender, "trap-not-found");
            return;
        }
        TrapChunk chunk = commandChunk((Player) sender, args);
        if (chunk == null) {
                    plugin.messages().send(sender, "admin-removechunk-usage");
            return;
        }
        if (trap.chunks().size() <= 1) {
            plugin.messages().send(sender, "chunk-last-remove");
            return;
        }
        if (!trap.chunks().remove(chunk)) {
            plugin.messages().send(sender, "chunk-not-in-trap");
            return;
        }
        plugin.traps().rebuildChunkCache();
        plugin.database().saveTrap(trap);
        plugin.traps().log(trap, (Player) sender, "CHUNK_REMOVE", "Chunk çıkarıldı: " + chunk.x() + ", " + chunk.z());
        plugin.holograms().update(trap);
        plugin.messages().send(sender, "chunk-removed", MessageManager.placeholders("%id%", trap.id(), "%chunks%", trap.chunks().size()));
    }

    private TrapChunk commandChunk(Player player, String[] args) {
        if (args.length == 2) {
            return TrapChunk.of(player.getLocation().getChunk());
        }
        return null;
    }

    private boolean requireId(CommandSender sender, String command, String[] args) {
        if (args.length >= 2) {
            return true;
        }
        plugin.messages().send(sender, "admin-id-usage", MessageManager.placeholders("%command%", command));
        return false;
    }

    private boolean requireValue(CommandSender sender, String command, String[] args) {
        if (args.length >= 3) {
            return true;
        }
        plugin.messages().send(sender, "admin-value-usage", MessageManager.placeholders("%command%", command));
        return false;
    }

    private void editOrMessage(CommandSender sender, String id, java.util.function.Consumer<TrapModel> consumer) {
        TrapModel trap = plugin.traps().byId(id).orElse(null);
        if (trap == null) {
            plugin.messages().send(sender, "trap-not-found");
            return;
        }
        consumer.accept(trap);
        plugin.database().saveTrap(trap);
        plugin.holograms().update(trap);
        plugin.messages().send(sender, "updated", MessageManager.placeholders("%id%", trap.id()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("trapachunk")) {
            if (args.length == 1) return new ArrayList<>(plugin.traps().traps().stream().map(TrapModel::id).toList());
            return Collections.emptyList();
        }
        if (command.getName().equalsIgnoreCase("qtrap")) {
            if (args.length == 1) return Arrays.asList("reload", "create", "delete", "setprice", "setowner", "addchunk", "removechunk", "sethealth", "setmaxhealth", "setlevel", "info");
            if (args.length == 2) return new ArrayList<>(plugin.traps().traps().stream().map(TrapModel::id).toList());
        }
        if (args.length == 1) {
            return Arrays.asList("al", "menu", "bilgi", "liste", "davet", "kabul", "reddet", "at", "rol", "banka", "para", "yükselt", "yukselt", "satış", "satis", "satışiptal", "satisiptal", "pazar", "spawn", "setspawn", "ziyaret", "sohbet", "pvp", "fly", "sınır", "sinir", "log", "kayıt", "kayit");
        }
        return Collections.emptyList();
    }
}
