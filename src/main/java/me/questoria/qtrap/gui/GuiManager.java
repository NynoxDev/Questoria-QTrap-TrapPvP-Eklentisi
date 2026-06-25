package me.questoria.qtrap.gui;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
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

import java.util.ArrayList;
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

        inventory.setItem(4, item(Material.NETHER_STAR, "&cTrap Menusu", "&7Trap islemlerini buradan yonet."));

        inventory.setItem(10, item(Material.EXPERIENCE_BOTTLE, "&cTrap Satin Al", "&7Bos trap bolgesini satin al."));
        inventory.setItem(11, item(Material.IRON_BLOCK, "&cTrap Bilgi", "&7Bulundugun trap bilgilerini goruntule."));
        inventory.setItem(12, item(Material.NAME_TAG, "&cTrap Listesi", "&7Sunucudaki trapleri listele."));
        inventory.setItem(13, item(Material.SLIME_BALL, "&cTrap Pazari", "&7Satistaki trapleri goruntule."));
        inventory.setItem(14, item(Material.OAK_DOOR, "&cZiyaret", "&7Trap ziyaret durumunu ac/kapat."));
        inventory.setItem(15, item(Material.IRON_SHOVEL, "&cTrap Fly", "&7Kendi trapinde fly kullan."));

        inventory.setItem(19, item(Material.DIAMOND_SWORD, "&cTrap Bilgi", "&7Bulundugun trap bilgisini goruntule."));
        inventory.setItem(20, item(Material.IRON_LEGGINGS, "&cUyeler", "&7Trap uyelerini yonet."));
        inventory.setItem(21, item(Material.OAK_DOOR, "&cZiyaret", "&7Ziyaret durumunu ac/kapat."));
        inventory.setItem(22, item(Material.RED_BED, "&cSpawn", "&7Trap spawnina isinlan."));
        inventory.setItem(23, item(Material.ENDER_PEARL, "&cSet Spawn", "&7Trap spawnini ayarla."));
        inventory.setItem(24, item(Material.EMERALD_BLOCK, "&cBanka", "&7Trap bankasini yonet."));
        inventory.setItem(25, item(Material.REDSTONE_BLOCK, "&cPvP", "&7PvP durumunu ac/kapat."));

        inventory.setItem(29, item(Material.SUGAR, "&cYukselt", "&7Trap seviyesini yukselt."));
        inventory.setItem(30, item(Material.COMPASS, "&cTrap Listesi", "&7Sunucudaki trapleri listele."));
        inventory.setItem(31, item(Material.BOOK, "&cPazar", "&7Satistaki trapleri goruntule."));
        inventory.setItem(32, item(Material.IRON_DOOR, "&cSatis Iptal", "&7Trap satisini iptal et."));
        inventory.setItem(33, item(Material.TNT, "&cTrap Sohbet", "&7Trap sohbetini ac/kapat."));
        inventory.setItem(40, item(Material.BARRIER, "&cKapat"));

        player.openInventory(inventory);
    }

    public void openList(Player player, int page, boolean market) {
        List<TrapModel> traps = market ? plugin.traps().marketTraps() : plugin.traps().sortedTraps();
        int pages = Math.max(1, (int) Math.ceil(traps.size() / (double) LIST_SLOTS.length));
        page = Math.max(1, Math.min(page, pages));
        QTrapHolder holder = new QTrapHolder(market ? GuiType.MARKET : GuiType.LIST, null, page);
        Inventory inventory = create(holder, 54, title(market ? "market" : "list", null, page, pages));
        fill(inventory);

        if (traps.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, market ? "&cPazarda trap yok" : "&cKayitli trap yok",
                    market ? "&7Satisa cikarilan trap bulunmuyor." : "&7Admin komutu: &f/qtrap create <id> <fiyat>"));
        }

        int start = (page - 1) * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < traps.size(); i++) {
            TrapModel trap = traps.get(start + i);
            Material material = trap.owned() ? (trap.forSale() ? Material.EMERALD_BLOCK : Material.GOLD_BLOCK) : Material.CHEST;
            inventory.setItem(LIST_SLOTS[i], item(material, "&c" + trap.name(),
                    "&7ID: &f" + trap.id(),
                    "&7Sahip: &f" + ownerName(trap),
                    "&7Seviye: &f" + trap.level(),
                    "&7Can: &f" + trap.health() + "/" + trap.maxHealth(),
                    trap.forSale() ? "&7Fiyat: &e" + plugin.vault().format(trap.salePrice()) : "&7Durum: &f" + (trap.owned() ? "Sahipli" : "Satilik"),
                    "&8Tiklayarak islem yap."));
        }

        inventory.setItem(45, item(Material.ARROW, "&cOnceki Sayfa"));
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

        inventory.setItem(0, playerHead(trap.owner(), "&c" + ownerName(trap), "&7Rol: &cSahip"));
        int start = (page - 1) * MEMBER_SLOTS.length;
        for (int i = 0; i < MEMBER_SLOTS.length && start + i < members.size(); i++) {
            TrapMember member = members.get(start + i);
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member.uuid());
            inventory.setItem(MEMBER_SLOTS[i], playerHead(member.uuid(), "&c" + safeName(offline),
                    "&7Rol: &f" + member.role().name(),
                    "&8Sol tik: rol menusu",
                    "&8Sag tik: cikar"));
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
        inventory.setItem(12, item(Material.REDSTONE, "&cCan", "&f" + trap.health() + "/" + trap.maxHealth()));
        inventory.setItem(14, item(Material.EMERALD, "&cBanka", "&f" + plugin.vault().format(trap.bankBalance())));
        inventory.setItem(16, item(Material.DIAMOND_SWORD, "&cDurum", "&7Seviye: &f" + trap.level(), "&7PvP: &f" + state(trap.pvp()), "&7Ziyaret: &f" + state(trap.visit())));
        inventory.setItem(22, item(Material.BONE, "&cChunklar", "&7Toplam: &f" + trap.chunks().size()));
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
        inventory.setItem(11, item(Material.NETHERITE_SWORD, "&cSahip", "&7Tum trap yetkileri."));
        inventory.setItem(12, item(Material.DIAMOND_SWORD, "&cYonetici", "&7Uye ve banka islemlerini yonetir."));
        inventory.setItem(13, item(Material.GOLDEN_SWORD, "&cModerator", "&7Temel trap islemlerine erisir."));
        inventory.setItem(14, item(Material.IRON_SWORD, "&cUye", "&7Trap icinde kullanici yetkileri."));
        inventory.setItem(15, item(Material.OAK_DOOR, "&cGeri Don", "&7Uye menusune don."));
        player.openInventory(inventory);
    }

    public void openBank(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.BANK, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("bank", trap, 1, 1));
        fill(inventory);
        inventory.setItem(11, item(Material.EMERALD, "&aPara Yatir", "&7Komut: &f/trap para yatir <miktar>"));
        inventory.setItem(13, item(Material.CHEST, "&cBakiye", "&f" + plugin.vault().format(trap.bankBalance()), "&7Limit: &f" + plugin.vault().format(plugin.configManager().bankLimit(trap.level()))));
        inventory.setItem(15, item(Material.GOLD_INGOT, "&ePara Cek", "&7Komut: &f/trap para cek <miktar>"));
        player.openInventory(inventory);
    }

    public void openConfirmBuy(Player player, TrapModel trap) {
        QTrapHolder holder = new QTrapHolder(GuiType.CONFIRM_BUY, trap.id(), 1);
        Inventory inventory = create(holder, 27, title("confirm-buy", trap, 1, 1));
        fill(inventory);
        inventory.setItem(11, item(Material.LIME_STAINED_GLASS_PANE, "&aOnayla", "&7Fiyat: &e" + plugin.vault().format(trap.salePrice())));
        inventory.setItem(13, item(Material.PAPER, "&f" + trap.name(), "&7Sahip: &f" + ownerName(trap), "&7Can: &f" + trap.health() + "/" + trap.maxHealth()));
        inventory.setItem(15, item(Material.RED_STAINED_GLASS_PANE, "&cIptal"));
        player.openInventory(inventory);
    }

    public ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageManager.color(name));
            ArrayList<String> colored = new ArrayList<>();
            for (String line : lore) {
                colored.add(MessageManager.color(line));
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
                "&7Uye Limiti: &f" + plugin.configManager().maxMembers(next),
                "&7Can: &f" + plugin.configManager().maxHealthForLevel(next),
                "&7Banka Limiti: &f" + plugin.vault().format(plugin.configManager().bankLimit(next)),
                "&8Tiklayarak yukselt.");
    }

    private Inventory create(QTrapHolder holder, int size, String title) {
        Inventory inventory = Bukkit.createInventory(holder, size, MessageManager.color(title));
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
        return state ? "Acik" : "Kapali";
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
