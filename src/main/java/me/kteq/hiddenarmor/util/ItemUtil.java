package me.kteq.hiddenarmor.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ItemUtil {

    private ItemUtil() {}

    public static ItemStack getArmor(ArmorSlot slot, PlayerInventory inv) {
        ItemStack item = switch (slot) {
            case HELMET -> inv.getHelmet();
            case CHEST -> inv.getChestplate();
            case LEGS -> inv.getLeggings();
            case BOOTS -> inv.getBoots();
        };
        return item != null ? item.clone() : new ItemStack(Material.AIR);
    }

    public enum ArmorSlot {
        HELMET(5), CHEST(6), LEGS(7), BOOTS(8);

        private final int slotId;

        ArmorSlot(int slotId) {
            this.slotId = slotId;
        }

        public int getSlotId() {
            return slotId;
        }
    }
}
