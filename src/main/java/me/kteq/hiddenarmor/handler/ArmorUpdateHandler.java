package me.kteq.hiddenarmor.handler;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.protocol.ProtocolUtil;
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
        for (int i = 5; i <= 8; i++) {
            ItemStack armor = ProtocolUtil.getArmor(ProtocolUtil.ArmorType.getType(i), inv);
            WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(
                    0, 0, i, SpigotConversionUtil.fromBukkitItemStack(armor)
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }

    public void updateToOthers(Player player) {
        PlayerInventory inv = player.getInventory();

        List<Equipment> equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(ProtocolUtil.getArmor(ProtocolUtil.ArmorType.HELMET, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(ProtocolUtil.getArmor(ProtocolUtil.ArmorType.CHEST, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(ProtocolUtil.getArmor(ProtocolUtil.ArmorType.LEGGS, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(ProtocolUtil.getArmor(ProtocolUtil.ArmorType.BOOTS, inv))));
        equipmentList.add(new Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(inv.getItemInMainHand().clone())));
        equipmentList.add(new Equipment(EquipmentSlot.OFF_HAND, SpigotConversionUtil.fromBukkitItemStack(inv.getItemInOffHand().clone())));

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), equipmentList);
        ProtocolUtil.broadcastPlayerPacket(packet, player);
    }
}
