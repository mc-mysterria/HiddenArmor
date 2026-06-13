package me.kteq.hiddenarmor.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

public final class ItemUtil {

    private ItemUtil() {}

    public static boolean isNotArmorPiece(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return true;
        String name = item.getType().name();
        return !name.endsWith("_HELMET") && !name.endsWith("_CHESTPLATE") &&
                !name.endsWith("_LEGGINGS") && !name.endsWith("_BOOTS") &&
                item.getType() != Material.ELYTRA && item.getType() != Material.TURTLE_HELMET;
    }

    public static int getDurabilityPercentage(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof Damageable meta)) return -1;
        int max = item.getType().getMaxDurability();
        if (max <= 0) return -1;
        return 100 - (meta.getDamage() * 100 / max);
    }

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

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

        public static ArmorSlot fromId(int id) {
            for (ArmorSlot slot : values()) {
                if (slot.slotId == id) return slot;
            }
            return null;
        }
    }
}
