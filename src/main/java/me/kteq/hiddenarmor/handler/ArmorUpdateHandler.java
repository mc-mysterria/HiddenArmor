package me.kteq.hiddenarmor.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class ArmorUpdateHandler {

    public ArmorUpdateHandler(HiddenArmor plugin) {
    }

    public void updatePlayer(Player player) {
        updateSelf(player);
        updateToOthers(player);
    }

    public void updateSelf(Player player) {
        sendSelf(player, false);
    }

    // Bypasses the placeholder listeners, so the client holds the real items before it gains creative authority over them.
    public void revealSelf(Player player) {
        sendSelf(player, true);
    }

    private void sendSelf(Player player, boolean silently) {
        PlayerInventory inv = player.getInventory();
        for (ItemUtil.ArmorSlot slot : ItemUtil.ArmorSlot.values()) {
            WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(
                    0, 0, slot.getSlotId(), SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(slot, inv))
            );
            if (silently) {
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
            } else {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }
    }

    public void updateToOthers(Player player) {
        if (player.getTrackedBy().isEmpty()) return;
        PlayerInventory inv = player.getInventory();

        List<Equipment> equipmentList = List.of(
                new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.HELMET, inv))),
                new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.CHEST, inv))),
                new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.LEGS, inv))),
                new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(ItemUtil.getArmor(ItemUtil.ArmorSlot.BOOTS, inv))),
                new Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(inv.getItemInMainHand())),
                new Equipment(EquipmentSlot.OFF_HAND, SpigotConversionUtil.fromBukkitItemStack(inv.getItemInOffHand()))
        );

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), equipmentList);
        for (Player viewer : player.getTrackedBy()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        }
    }
}
