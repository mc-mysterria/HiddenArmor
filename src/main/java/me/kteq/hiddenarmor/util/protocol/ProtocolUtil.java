package me.kteq.hiddenarmor.util.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ProtocolUtil {

    public static void broadcastPlayerPacket(PacketWrapper<?> packet, Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().equals(player.getWorld())) continue;
            if (p.getLocation().distance(player.getLocation()) >= Bukkit.getViewDistance() * 16) continue;
            if (p.equals(player)) continue;
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
        }
    }

    public static ItemStack getArmor(ArmorType type, PlayerInventory inv) {
        switch (type) {
            case HELMET:
                if (inv.getHelmet() != null) return inv.getHelmet().clone();
                break;
            case CHEST:
                if (inv.getChestplate() != null) return inv.getChestplate().clone();
                break;
            case LEGGS:
                if (inv.getLeggings() != null) return inv.getLeggings().clone();
                break;
            case BOOTS:
                if (inv.getBoots() != null) return inv.getBoots().clone();
                break;
        }
        return new ItemStack(Material.AIR);
    }

    public enum ArmorType {
        HELMET(5), CHEST(6), LEGGS(7), BOOTS(8);

        private final int value;

        ArmorType(int i) {
            this.value = i;
        }

        public static ArmorType getType(int value) {
            for (ArmorType type : values()) {
                if (type.getValue() == value) return type;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }
}
