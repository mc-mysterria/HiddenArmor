package me.kteq.hiddenarmor.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorPlaceholderHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.entity.Player;

import java.util.List;

public class EntityEquipmentPacketListener extends PacketListenerAbstract {

    private final PlayerManager playerManager;
    private final ArmorPlaceholderHandler placeholderHandler;

    public EntityEquipmentPacketListener(HiddenArmor plugin) {
        this.playerManager = plugin.getPlayerManager();
        this.placeholderHandler = plugin.getArmorPlaceholderHandler();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) return;

        WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
        Player packetPlayer = playerManager.getByEntityId(wrapper.getEntityId());
        if (packetPlayer == null || playerManager.isArmorVisible(packetPlayer)) return;

        List<Equipment> equipment = wrapper.getEquipment();
        for (Equipment eq : equipment) {
            if (eq.getSlot() == EquipmentSlot.MAIN_HAND || eq.getSlot() == EquipmentSlot.OFF_HAND || eq.getItem().isEmpty()) continue;
            if (placeholderHandler.shouldHide(SpigotConversionUtil.toBukkitItemStack(eq.getItem()), packetPlayer)) {
                eq.setItem(ItemStack.EMPTY);
            }
        }
        wrapper.setEquipment(equipment);
    }
}
