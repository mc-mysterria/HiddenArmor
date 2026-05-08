package me.kteq.hiddenarmor.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorPlaceholderHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetSlotPacketListener extends PacketListenerAbstract {
    private final PlayerManager playerManager;
    private final ArmorPlaceholderHandler placeholderHandler;

    public SetSlotPacketListener(HiddenArmor plugin) {
        this.playerManager = plugin.getPlayerManager();
        this.placeholderHandler = plugin.getArmorPlaceholderHandler();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SET_SLOT) return;

        Player player = Bukkit.getPlayer(event.getUser().getUUID());
        if (player == null || playerManager.isArmorVisible(player)) return;

        WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);
        if (wrapper.getWindowId() != 0) return;

        int slot = wrapper.getSlot();
        if (slot < 5 || slot > 8) return;

        com.github.retrooper.packetevents.protocol.item.ItemStack peItem = wrapper.getItem();
        if (peItem != null && !peItem.isEmpty()) {
            ItemStack bukkitItem = SpigotConversionUtil.toBukkitItemStack(peItem);
            ItemStack placeholder = placeholderHandler.buildItemPlaceholder(bukkitItem);
            wrapper.setItem(SpigotConversionUtil.fromBukkitItemStack(placeholder));
        }
    }
}
