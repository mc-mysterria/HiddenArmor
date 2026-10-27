package me.kteq.hiddenarmor.listener;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorUpdateHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryShiftClickListener implements Listener {

    private final HiddenArmor plugin;

    private final PlayerManager hiddenArmorManager;
    private final ArmorUpdateHandler armorUpdater;

    public InventoryShiftClickListener(HiddenArmor plugin) {
        this.plugin = plugin;
        this.hiddenArmorManager = plugin.getPlayerManager();
        this.armorUpdater = plugin.getArmorUpdater();
    }

    // The client predicts armor moves (shift-click, number keys, cursor swaps) and the server won't resend a slot it thinks is in sync.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorSlotChange(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING) return;
        if (!event.isShiftClick() && event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (hiddenArmorManager.isArmorVisible(player)) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> armorUpdater.updateSelf(player));
    }
}
