package me.kteq.hiddenarmor.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorPlaceholderHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WindowItemsPacketListener extends PacketListenerAbstract {
    private final PlayerManager playerManager;
    private final ArmorPlaceholderHandler placeholderHandler;

    public WindowItemsPacketListener(HiddenArmor plugin) {
        this.playerManager = plugin.getPlayerManager();
        this.placeholderHandler = plugin.getArmorPlaceholderHandler();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.WINDOW_ITEMS) return;

        Player player = Bukkit.getPlayer(event.getUser().getUUID());
        if (player == null || playerManager.isArmorVisible(player)) return;

        WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event);
        if (wrapper.getWindowId() != 0) return;

        List<ItemStack> items = new ArrayList<>(wrapper.getItems());
        for (int i = 5; i < 9; i++) {
            ItemStack peItem = items.get(i);
            if (peItem != null && !peItem.isEmpty()) {
                org.bukkit.inventory.ItemStack bukkitItem = SpigotConversionUtil.toBukkitItemStack(peItem);
                org.bukkit.inventory.ItemStack placeholder = placeholderHandler.buildItemPlaceholder(bukkitItem);
                items.set(i, SpigotConversionUtil.fromBukkitItemStack(placeholder));
            }
        }
        wrapper.setItems(items);
    }
}
