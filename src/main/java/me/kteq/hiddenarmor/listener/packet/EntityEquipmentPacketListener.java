package me.kteq.hiddenarmor.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.manager.PlayerManager;
import me.kteq.hiddenarmor.util.ConfigHolder;
import me.kteq.hiddenarmor.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityEquipmentPacketListener extends PacketListenerAbstract implements ConfigHolder {

    private final PlayerManager hiddenArmorManager;

    private boolean ignoreLeatherArmor;
    private boolean ignoreTurtleHelmet;
    private boolean ignoreElytra;

    public EntityEquipmentPacketListener(HiddenArmor plugin) {
        plugin.addConfigHolder(this);
        this.hiddenArmorManager = plugin.getPlayerManager();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) return;

        Player receiver = Bukkit.getPlayer(event.getUser().getUUID());
        if (receiver == null) return;

        WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
        int entityId = wrapper.getEntityId();

        Player packetPlayer = Bukkit.getOnlinePlayers().stream()
                .filter(e -> e.getEntityId() == entityId)
                .findFirst().orElse(null);

        if (packetPlayer == null) return;

        if (hiddenArmorManager.isArmorVisible(packetPlayer)) return;

        List<Equipment> equipment = wrapper.getEquipment();
        for (Equipment eq : equipment) {
            ItemStack item = SpigotConversionUtil.toBukkitItemStack(eq.getItem());

            if (item.getType() == Material.ELYTRA
                    && (packetPlayer.isGliding() || ignoreElytra)
                    && !packetPlayer.isInvisible()) {
                eq.setItem(SpigotConversionUtil.fromBukkitItemStack(new ItemStack(Material.ELYTRA)));
            } else if (!shouldIgnore(item, eq.getSlot())) {
                eq.setItem(SpigotConversionUtil.fromBukkitItemStack(new ItemStack(Material.AIR)));
            }
        }
        wrapper.setEquipment(equipment);
    }

    private boolean shouldIgnore(ItemStack itemStack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAIN_HAND || slot == EquipmentSlot.OFF_HAND) return true;

        Material material = itemStack.getType();
        return (ignoreLeatherArmor && material.toString().startsWith("LEATHER")) ||
                (ignoreTurtleHelmet && material == Material.TURTLE_HELMET) ||
                (ItemUtil.isNotArmorPiece(itemStack) && material != Material.ELYTRA) ||
                (ignoreElytra && material == Material.ELYTRA);
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.ignoreLeatherArmor = config.getBoolean("ignore.leather-armor");
        this.ignoreTurtleHelmet = config.getBoolean("ignore.turtle-helmet");
        this.ignoreElytra = config.getBoolean("ignore.elytra");
    }
}
