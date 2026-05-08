package me.kteq.hiddenarmor.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class ArmorUpdateHandler {

    public ArmorUpdateHandler(HiddenArmor plugin) {
    }

    public void updatePlayer(Player player) {
        updateSelf(player);
        updateToOthers(player);
    }

    public void updateSelf(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ItemUtil.ArmorSlot slot : ItemUtil.ArmorSlot.values()) {
            ItemStack armor = ItemUtil.getArmor(slot, inv);
            WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(
                    0, 0, slot.getSlotId(), SpigotConversionUtil.fromBukkitItemStack(armor)
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }

    public void updateToOthers(Player player) {
        PlayerInventory inv = player.getInventory();

        List<Equipment> equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.HELMET, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.CHEST, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.LEGS, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.BOOTS, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(inv.getItemInMainHand().clone())));
        equipmentList.add(new Equipment(EquipmentSlot.OFF_HAND, SpigotConversionUtil.fromBukkitItemStack(inv.getItemInOffHand().clone())));

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), equipmentList);
        broadcastPlayerPacket(packet, player);
    }

    private void broadcastPlayerPacket(PacketWrapper<?> packet, Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().equals(player.getWorld())) continue;
            if (p.getLocation().distanceSquared(player.getLocation()) > 16384) continue; // 128 blocks squared
            if (p.equals(player)) continue;
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
        }
    }
}
