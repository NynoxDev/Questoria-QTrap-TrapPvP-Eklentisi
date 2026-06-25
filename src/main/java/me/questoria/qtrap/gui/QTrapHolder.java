package me.questoria.qtrap.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class QTrapHolder implements InventoryHolder {
    private final GuiType type;
    private final String trapId;
    private final int page;
    private Inventory inventory;

    public QTrapHolder(GuiType type, String trapId, int page) {
        this.type = type;
        this.trapId = trapId;
        this.page = page;
    }

    public GuiType type() {
        return type;
    }

    public String trapId() {
        return trapId;
    }

    public int page() {
        return page;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
