package me.kteq.hiddenarmor;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.kteq.hiddenarmor.command.HiddenArmorCommand;
import me.kteq.hiddenarmor.command.HiddenArmorTabCompleter;
import me.kteq.hiddenarmor.command.ToggleArmorCommand;
import me.kteq.hiddenarmor.handler.ArmorPlaceholderHandler;
import me.kteq.hiddenarmor.handler.ArmorUpdateHandler;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.listener.EntityToggleGlideListener;
import me.kteq.hiddenarmor.listener.GameModeListener;
import me.kteq.hiddenarmor.listener.InventoryShiftClickListener;
import me.kteq.hiddenarmor.listener.PotionEffectListener;
import me.kteq.hiddenarmor.listener.packet.EntityEquipmentPacketListener;
import me.kteq.hiddenarmor.listener.packet.SetSlotPacketListener;
import me.kteq.hiddenarmor.listener.packet.WindowItemsPacketListener;
import me.kteq.hiddenarmor.manager.PlayerManager;
import me.kteq.hiddenarmor.util.ConfigHolder;
import me.kteq.hiddenarmor.util.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class HiddenArmor extends JavaPlugin {
    private PlayerManager playerManager;
    private ArmorUpdateHandler armorUpdater;
    private ArmorPlaceholderHandler armorPlaceholderHandler;
    private MessageHandler messageHandler;

    private List<ConfigHolder> configHolders;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        checkConfig();

        this.messageHandler = new MessageHandler(this, "&c[&fHiddenArmor&c] &f");
        this.armorUpdater = new ArmorUpdateHandler(this);
        this.playerManager = new PlayerManager(this);
        this.armorPlaceholderHandler = new ArmorPlaceholderHandler(this);

        new ToggleArmorCommand(this, "togglearmor")
                .setPermission("hiddenarmor")
                .setPermissionRequired(false);
        new HiddenArmorCommand(this, "hiddenarmor")
                .setPermission("hiddenarmor")
                .setPermissionRequired(false)
                .setTabCompleter(new HiddenArmorTabCompleter(this));

        PacketEvents.getAPI().init();

        PacketEvents.getAPI().getEventManager().registerListeners(
                new SetSlotPacketListener(this),
                new WindowItemsPacketListener(this),
                new EntityEquipmentPacketListener(this)
        );

        new InventoryShiftClickListener(this);
        new GameModeListener(this);
        new PotionEffectListener(this);
        new EntityToggleGlideListener(this);

        reloadConfig();

        new Metrics(this, 14419);
    }

    @Override
    public void onDisable() {
        playerManager.saveCurrentEnabledPlayers();
        PacketEvents.getAPI().terminate();
    }

    private void checkConfig() {
        reloadConfig();
        if (getConfig().getInt("config-version") >= getConfig().getDefaults().getInt("config-version"))
            return;
        getLogger().log(Level.WARNING, "Your HiddenArmor configuration file is outdated!");
        getLogger().log(Level.WARNING, "Please regenerate the 'config.yml' file when possible.");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configHolders == null)
            configHolders = new ArrayList<>();
        configHolders.forEach(c -> c.loadConfig(getConfig()));
    }

    public void addConfigHolder(ConfigHolder configHolder) {
        configHolders.add(configHolder);
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ArmorUpdateHandler getArmorUpdater() {
        return armorUpdater;
    }

    public ArmorPlaceholderHandler getArmorPlaceholderHandler() {
        return armorPlaceholderHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
