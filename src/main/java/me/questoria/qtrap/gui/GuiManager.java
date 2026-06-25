package me.questoria.qtrap.gui;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.model.TrapLogEntry;
import me.questoria.qtrap.model.TrapMember;
import me.questoria.qtrap.model.TrapModel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class GuiManager {
    public static final int[] LIST_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    private static final int[] MEMBER_SLOTS = {
            1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    private final QTrapPlugin plugin;

    public GuiManager(QTrapPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        QTrapHolder holder = new QTrapHolder(GuiType.MAIN, null, 1);
        Inventory inventory = create(holder, 45, title("main", null, 1, 1));
        fill(inventory);

        inventory.setItem(4, item(Material.NETHER_STAR, "&c&lQTrap", "&7Trap işlemlerini tek menüden yönet.", "", "&8Questoria Studio"));

        inventory.setItem(10, item(Material.EXPERIENCE_BOTTLE, "&cTrap Satın Al", "&7Boş trap bölgesini satın al.", "&8Tıklayarak işlem yap."));
        inventory.setItem(11, item(Material.IRON_BLOCK, "&cTrap Bilgi", "&7Bulunduğun trap detaylarını görüntüle."));
        inventory.setItem(12, item(Material.NAME_TAG, "&cTrap Listesi", "&7Sunucudaki tüm trapleri listele."));
        inventory.setItem(13, item(Material.SLIME_BALL, "&cTrap Pazarı", "&7Satıştaki trapleri görüntüle."));
        inventory.setItem(14, item(Material.OAK_DOOR, "&cZiyaret", "&7Trap ziyaret durumunu aç/kapat."));
        inventory.setItem(15, item(Material.FEATHER, "&cTrap Fly", "&7Kendi trapinde uçuş modunu kullan."));

        inventory.setItem(19, item(Material.DIAMOND_SWORD, "&cBölge Bilgisi", "&7Bulunduğun trap bilgisini görüntüle."));
        inventory.setItem(20, item(Material.PLAYER_HEAD, "&cÜyeler", "&7Trap üyelerini ve rollerini yönet."));
        inventory.setItem(21, item(Material.OAK_DOOR, "&cZiyaret Ayarı", "&7Ziyaret durumunu aç/kapat."));
        inventory.setItem(22, item(Material.RED_BED, "&cTrap Spawn", "&7Trap spawnına ışınlan."));
        inventory.setItem(23, item(Material.ENDER_PEARL, "&cSpawn Belirle", "&7Trap spawn noktasını ayarla."));
        inventory.setItem(24, item(Material.EMERALD_BLOCK, "&cTrap Bankası", "&7Ortak trap bakiyesini yönet."));
        inventory.setItem(25, item(Material.REDSTONE_BLOCK, "&cPvP Ayarı", "&7PvP durumunu aç/kapat."));

        inventory.setItem(29, item(Material.TNT_MINECART, "&cTrap Yükselt", "&7Seviye, can ve limitleri artır."));
        inventory.setItem(30, item(Material.COMPASS, "&cTrapler", "&7Tüm trapleri sayfalı şekilde görüntüle."));
        inventory.setItem(31, item(Material.BOOK, "&cPazar", "&7Satıştaki trapleri incele."));
        inventory.setItem(32, item(Material.IRON_DOOR, "&cSatış İptal", "&7Trap satışını iptal et."));
        inventory.setItem(33, item(Material.WRITABLE_BOOK, "&cTrap Sohbet", "&7Trap sohbetini aç/kapat."));
        inventory.setItem(34, item(Material.CLOCK, "&cAktivite Logları", "&7Trap geçmişini görüntüle."));
        inventory.setItem(40, item(Material.BARRIER, "&cKapat"));

        player.openInventory(inventory);
    }

    public void openList(Player player, int page, boolean market) {
        openList(player, page, market, 0);
    }

    public void openList(Player player, int page, boolean market, int mode) {
        List<TrapModel> traps = market ? plugin.traps().marketTraps() : plugin.traps().sortedTraps();
        if (market) {
            traps = new ArrayList<>(traps);
            switch (mode) {
                case 1 -> traps.sort(Comparator.<TrapModel>comparingInt(trap -> trap.level()).reversed());
                case 2 -> traps.sort(Comparator.<TrapModel>comparingInt(trap -> trap.health()).reversed());
                default -> traps.sort(Comparator.comparingDouble(trap -> trap.salePrice()));
            }
        }
        int pages = Math.max(1, (int) Math.ceil(traps.size() / (double) LIST_SLOTS.length));
        page = Math.max(1, Math.min(page, pages));
        QTrapHolder holder = new QTrapHolder(market ? GuiType.MARKET : GuiType.LIST, null, page, mode);
        Inventory inventory = create(holder, 54, title(market ? "market" : "list", null, page, pages));
        fill(inventory);

        if (traps.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, market ? "&cPazarda trap yok" : "&cKayıtlı trap yok",
                    market ? "&7Satışa çıkarılan trap bulunmuyor." : "&7Admin komutu: &f/qtrap create <id> <fiyat>"));
        }

        int start = (page - 1) * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < traps.size(); i++) {
            TrapModel trap = traps.get(start + i);
            Material material = trap.owned() ? (trap.forSale() ? Material.EMERALD_BLOCK : Material.GOLD_BLOCK) : Material.CHEST;
            inventory.setItem(LIST_SLOTS[i], item(material, "&c" + trap.name(),
                    "&7ID: &f" + trap.id(),
                    "&7Sahip: &f" + ownerName(trap),
                    "&7Seviye: &c" + trap.level(),
                    "&7Can: &c" + trap.health() + "&7/&c" + trap.maxHealth(),
                    trap.forSale() ? "&7Fiyat: &e" + plugin.vault().format(trap.salePrice()) : "&7Durum: &f" + (trap.owned() ? "Sahipli" : "Satılık"),
                    "",
                    "&8Tıklayarak işlem yap."));
        }

        inventory.setItem(45, item(Material.ARROW, "&cOnceki Sayfa"));
        if (market) {
            inventory.setItem(46, item(mode == 0 ? Material.HOPPER_MINECART : Material.HOPPER, "&cFiyat Sıralaması", "&7En düşük fiyattan başlar.", active(mode == 0)));
            inventory.setItem(47, item(mode == 1 ? Material.EXPERIENCE_BOTTLE : Material.GLASS_BOTTLE, "&cSeviye Sıralaması", "&7Yüksek seviyeli trapleri öne alır.", active(mode == 1)));
            inventory.setItem(51, item(mode == 2 ? Material.REDSTONE_BLOCK : Material.REDSTONE, "&cCan Sıralaması", "&7Canı yüksek trapleri öne alır.", active(mode == 2)));
        }
        inventory.setItem(49, item(Material.PAPER, "&fSayfa &c" + page + "&7/&c" + pages));
        inventory.setItem(53, item(Material.ARROW, "&cSonraki Sayfa"));
        player.openInventory(inventory);
    }

    public void openMembers(Player player, TrapModel trap, int page) {
        List<TrapMember> members = new ArrayList<>(trap.memberValues());
        int pages = Math.max(1, (int) Math.ceil(Math.max(1, members.size()) / (double) MEMBER_SLOTS.length));
        page = Math.max(1, Math.min(page, pages));
        QTrapHolder holder = new QTrapHolder(GuiType.MEMBERS, trap.id(), page);
        Inventory inventory = create(holder, 54, title("members", trap, page, pages));
        fill(inventory);

        inventory.setItem(0, playerHead(trap.owner(), "&c" + ownerName(trap), "&7Rol: &cSahip", "&8Trap sahibi"));
        int start = (page - 1) * MEMBER_SLOTS.length;
        for (int i = 0; i < MEMBER_SLOTS.length && start + i < members.size(); i++) {
            TrapMember member = members.get(start + i);
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member.uuid());
            inventory.setItem(MEMBER_SLOTS[i], playerHead(member.uuid(), roleColor(member.role()) + safeName(offline),
                    "&7Rol: " + roleColor(member.role()) + roleName(member.role()),
                    "",
                    "&8Sol tık: rol menüsü",
                    "&8Sağ tık: çıkar"));
        }

        inventory.setItem(45, item(Material.ARROW, "&cOnceki Sayfa"));
        inventory.setItem(49, item(Material.PAPER, "&fSayfa &c" + page + "&7/&c" + pages));
        inventory.setItem(53, item(Material.ARROW, "&cSonraki Sayfa"));
        player.openInventory(inventory);
    }

    public void openInfo(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.INFO, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("info", trap, 1, 1));
        fill(inventory);
        inventory.setItem(10, playerHead(trap.owner(), "&cSahip", "&f" + ownerName(trap)));
        inventory.setItem(12, item(Material.REDSTONE, "&cTrap Canı", "&f" + trap.health() + "&7/&f" + trap.maxHealth()));
        inventory.setItem(14, item(Material.EMERALD, "&cTrap Bankası", "&f" + plugin.vault().format(trap.bankBalance())));
        inventory.setItem(16, item(Material.DIAMOND_SWORD, "&cDurum", "&7Seviye: &c" + trap.level(), "&7PvP: &f" + state(trap.pvp()), "&7Ziyaret: &f" + state(trap.visit())));
        inventory.setItem(20, item(Material.MAP, "&cTrap Haritası", "&7Dünya: &f" + firstWorld(trap), "&7Chunk: &f" + trap.chunks().size(), "&7Sınır: &f/trap sınır"));
        inventory.setItem(22, item(Material.BONE, "&cChunklar", "&7Toplam: &f" + trap.chunks().size(), "&7Limit: &f" + plugin.configManager().chunkLimit(trap.level())));
        inventory.setItem(24, item(Material.NAME_TAG, "&cVitrin Bilgisi", "&7Durum: &f" + marketState(trap), trap.forSale() ? "&7Fiyat: &e" + plugin.vault().format(trap.salePrice()) : "&7Pazar: &cKapalı"));
        player.openInventory(inventory);
    }

    public void openUpgrade(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.UPGRADE, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("upgrade", trap, 1, 1));
        fill(inventory);
        int next = trap.level() + 1;
        double price = plugin.configManager().upgradePrice(next);
        inventory.setItem(11, item(Material.CHEST_MINECART, "&cMevcut Seviye", "&7Seviye: &f" + trap.level(), "&7Can: &f" + trap.health() + "/" + trap.maxHealth()));
        inventory.setItem(12, upgradeItem(trap, next, price));
        inventory.setItem(13, upgradeItem(trap, next, price));
        inventory.setItem(14, upgradeItem(trap, next, price));
        inventory.setItem(15, upgradeItem(trap, next, price));
        player.openInventory(inventory);
    }

    public void openRoles(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.ROLES, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("roles", trap, 1, 1));
        fill(inventory);
        inventory.setItem(11, item(Material.NETHERITE_SWORD, "&cSahip", "&7Tüm trap yetkilerine sahiptir."));
        inventory.setItem(12, item(Material.DIAMOND_SWORD, "&cYönetici", "&7Üye ve banka işlemlerini yönetir."));
        inventory.setItem(13, item(Material.GOLDEN_SWORD, "&cModeratör", "&7Temel trap işlemlerine erişir."));
        inventory.setItem(14, item(Material.IRON_SWORD, "&cÜye", "&7Trap içinde kullanıcı yetkileri."));
        inventory.setItem(15, item(Material.OAK_DOOR, "&cGeri Dön", "&7Üye menüsüne dön."));
        player.openInventory(inventory);
    }

    public void openBank(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.BANK, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("bank", trap, 1, 1));
        fill(inventory);
        inventory.setItem(11, item(Material.EMERALD, "&aPara Yatır", "&7Komut: &f/trap para yatır <miktar>"));
        inventory.setItem(13, item(Material.CHEST, "&cBakiye", "&f" + plugin.vault().format(trap.bankBalance()), "&7Limit: &f" + plugin.vault().format(plugin.configManager().bankLimit(trap.level()))));
        inventory.setItem(15, item(Material.GOLD_INGOT, "&ePara Çek", "&7Komut: &f/trap para çek <miktar>"));
        player.openInventory(inventory);
    }

    public void openLogs(Player player, TrapModel trap, List<TrapLogEntry> logs) {
        QTrapHolder holder = new QTrapHolder(GuiType.LOGS, trap.id(), 1);
        Inventory inventory = create(holder, 54, title("logs", trap, 1, 1));
        fill(inventory);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM HH:mm");
        int slot = 10;
        for (TrapLogEntry log : logs) {
            if (slot > 43) {
                break;
            }
            inventory.setItem(slot, item(Material.PAPER, "&c" + actionName(log.action()),
                    "&7Oyuncu: &f" + (log.actorName() == null ? "Sistem" : log.actorName()),
                    "&7Tarih: &f" + format.format(new Date(log.createdAt())),
                    "",
                    "&f" + log.detail()));
            slot++;
            if (slot == 17 || slot == 26 || slot == 35) {
                slot += 2;
            }
        }
        if (logs.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, "&cKayıt yok", "&7Bu trap için henüz aktivite kaydı oluşmamış."));
        }
        inventory.setItem(49, item(Material.OAK_DOOR, "&cGeri Dön", "&7Ana menüye dön."));
        player.openInventory(inventory);
    }

    public void openConfirmBuy(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.CONFIRM_BUY, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("confirm-buy", trap, 1, 1));
        fill(inventory);
        inventory.setItem(11, item(Material.LIME_STAINED_GLASS_PANE, "&aOnayla", "&7Fiyat: &e" + plugin.vault().format(trap.salePrice())));
        inventory.setItem(13, item(Material.PAPER, "&f" + trap.name(), "&7Sahip: &f" + ownerName(trap), "&7Can: &f" + trap.health() + "/" + trap.maxHealth()));
        inventory.setItem(15, item(Material.RED_STAINED_GLASS_PANE, "&cİptal"));
        player.openInventory(inventory);
    }

    public ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageManager.color(guiText(name)));
            ArrayList<String> colored = new ArrayList<>();
            for (String line : lore) {
                colored.add(MessageManager.color(guiText(line)));
            }
            meta.setLore(colored);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack upgradeItem(TrapModel trap, int next, double price) {
        return item(Material.TNT, "&cSeviye " + next,
                "&7Fiyat: &e" + plugin.vault().format(price),
                "&7Üye Limiti: &f" + plugin.configManager().maxMembers(next),
                "&7Can: &f" + plugin.configManager().maxHealthForLevel(next),
                "&7Banka Limiti: &f" + plugin.vault().format(plugin.configManager().bankLimit(next)),
                "",
                "&8Tıklayarak yükselt.");
    }

    private Inventory create(QTrapHolder holder, int size, String title) {
        Inventory inventory = Bukkit.createInventory(holder, size, MessageManager.color(guiText(title)));
        holder.inventory(inventory);
        return inventory;
    }

    private void fill(Inventory inventory) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE"));
        ItemStack filler = item(material == null ? Material.GRAY_STAINED_GLASS_PANE : material, plugin.getConfig().getString("gui.filler.name", " "));
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private String title(String key, TrapModel trap, int page, int pages) {
        return sanitize(plugin.getConfig().getString("gui.titles." + key, key)
                .replace("%id%", trap == null ? "" : trap.id())
                .replace("%page%", String.valueOf(page))
                .replace("%pages%", String.valueOf(pages)));
    }

    private ItemStack playerHead(java.util.UUID uuid, String name, String... lore) {
        return item(Material.PLAYER_HEAD, name, lore);
    }

    private String ownerName(TrapModel trap) {
        return trap.owner() == null ? "Yok" : safeName(Bukkit.getOfflinePlayer(trap.owner()));
    }

    private String safeName(OfflinePlayer player) {
        return player.getName() == null ? player.getUniqueId().toString().substring(0, 8) : player.getName();
    }

    private String state(boolean state) {
        return state ? "&aAçık" : "&cKapalı";
    }

    private String active(boolean active) {
        return active ? "&aŞu anda aktif." : "&8Tıklayarak seç.";
    }

    private String actionName(String action) {
        return switch (action) {
            case "BUY" -> "Satın Alma";
            case "CREATE" -> "Oluşturma";
            case "DELETE" -> "Silme";
            case "MEMBER_JOIN" -> "Üye Katıldı";
            case "MEMBER_KICK" -> "Üye Çıkarıldı";
            case "ROLE_CHANGE" -> "Rol Değişti";
            case "UPGRADE" -> "Yükseltme";
            case "DAMAGE" -> "Hasar";
            case "DISBAND" -> "Dağılma";
            case "BANK_DEPOSIT" -> "Banka Yatırma";
            case "BANK_WITHDRAW" -> "Banka Çekme";
            case "SALE" -> "Satış";
            case "SALE_CANCEL" -> "Satış İptal";
            case "SPAWN_SET" -> "Spawn Ayarı";
            case "PVP_TOGGLE" -> "PvP Ayarı";
            case "VISIT_TOGGLE" -> "Ziyaret Ayarı";
            case "CHUNK_ADD" -> "Chunk Ekleme";
            case "CHUNK_REMOVE" -> "Chunk Çıkarma";
            default -> action;
        };
    }

    private String roleColor(me.questoria.qtrap.model.TrapRole role) {
        return switch (role) {
            case OWNER -> "&c";
            case MANAGER -> "&6";
            case MODERATOR -> "&e";
            case MEMBER -> "&a";
        };
    }

    private String roleName(me.questoria.qtrap.model.TrapRole role) {
        return switch (role) {
            case OWNER -> "Sahip";
            case MANAGER -> "Yönetici";
            case MODERATOR -> "Moderatör";
            case MEMBER -> "Üye";
        };
    }

    private String marketState(TrapModel trap) {
        if (trap.forSale()) {
            return "&eSatılık";
        }
        return trap.owned() ? "&cSahipli" : "&aSatın Alınabilir";
    }

    private String firstWorld(TrapModel trap) {
        return trap.chunks().isEmpty() ? "-" : trap.chunks().iterator().next().world();
    }

    private String guiText(String input) {
        if (!plugin.getConfig().getBoolean("gui.small-font", true)) {
            return input;
        }
        return smallFont(input);
    }

    private String smallFont(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder output = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if ((current == '&' || current == '§') && i + 1 < input.length()) {
                output.append(current).append(input.charAt(++i));
                continue;
            }
            output.append(smallChar(current));
        }
        return output.toString();
    }

    private char smallChar(char character) {
        return switch (Character.toLowerCase(character)) {
            case 'a', 'á' -> 'ᴀ';
            case 'b' -> 'ʙ';
            case 'c', 'ç' -> 'ᴄ';
            case 'd' -> 'ᴅ';
            case 'e' -> 'ᴇ';
            case 'f' -> 'ꜰ';
            case 'g', 'ğ' -> 'ɢ';
            case 'h' -> 'ʜ';
            case 'i', 'ı', 'İ' -> 'ɪ';
            case 'j' -> 'ᴊ';
            case 'k' -> 'ᴋ';
            case 'l' -> 'ʟ';
            case 'm' -> 'ᴍ';
            case 'n' -> 'ɴ';
            case 'o', 'ö' -> 'ᴏ';
            case 'p' -> 'ᴘ';
            case 'q' -> 'ǫ';
            case 'r' -> 'ʀ';
            case 's', 'ş' -> 'ꜱ';
            case 't' -> 'ᴛ';
            case 'u', 'ü' -> 'ᴜ';
            case 'v' -> 'ᴠ';
            case 'w' -> 'ᴡ';
            case 'x' -> 'x';
            case 'y' -> 'ʏ';
            case 'z' -> 'ᴢ';
            default -> character;
        };
    }

    private String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("ÃƒÅ“", "U")
                .replace("ÃƒÂ¼", "u")
                .replace("Ãƒâ€“", "O")
                .replace("ÃƒÂ¶", "o")
                .replace("Ã„Â°", "I")
                .replace("Ã„Â±", "i")
                .replace("Ã…Å¾", "S")
                .replace("Ã…Å¸", "s")
                .replace("Ã„Å¸", "g")
                .replace("Ãƒâ€¡", "C")
                .replace("ÃƒÂ§", "c");
    }
}
