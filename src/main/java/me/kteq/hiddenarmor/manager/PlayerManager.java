package me.kteq.hiddenarmor.manager;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorUpdateHandler;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.util.ConfigHolder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerManager implements ConfigHolder {

    private final HiddenArmor plugin;

    private final ArmorUpdateHandler armorUpdater;
    private final MessageHandler messageHandler;

    // read from netty threads by the packet listeners
    private final Set<UUID> enabledPlayersUUID = ConcurrentHashMap.newKeySet();
    private final Map<Integer, Player> playersByEntityId = new ConcurrentHashMap<>();
    private final Map<UUID, Long> combatUntil = new ConcurrentHashMap<>();

    private File enabledPlayersFile;
    private FileConfiguration enabledPlayersConfig;
    private boolean dirty;

    private boolean invisibleAlwaysHideGear;
    private long combatRevealMillis;

    public PlayerManager(HiddenArmor plugin) {
        this.plugin = plugin;
        plugin.addConfigHolder(this);
        this.armorUpdater = plugin.getArmorUpdater();
        this.messageHandler = plugin.getMessageHandler();
        loadEnabledPlayers();
        Bukkit.getOnlinePlayers().forEach(this::track);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void togglePlayer(Player player, boolean inform) {
        if (isEnabled(player)) {
            disablePlayer(player, inform);
        } else {
            enablePlayer(player, inform);
        }
    }

    public void enablePlayer(Player player, boolean inform) {
        if (!enabledPlayersUUID.add(player.getUniqueId())) return;
        if (inform) {
            messageHandler.sendActionBar(player, "%armor-visibility%", Map.of("visibility", "%visibility-hidden%"));
        }
        dirty = true;
        armorUpdater.updatePlayer(player);
    }

    public void disablePlayer(Player player, boolean inform) {
        if (!enabledPlayersUUID.remove(player.getUniqueId())) return;
        if (inform) {
            messageHandler.sendActionBar(player, "%armor-visibility%", Map.of("visibility", "%visibility-shown%"));
        }
        dirty = true;
        armorUpdater.updatePlayer(player);
    }

    public boolean isEnabled(Player player) {
        return enabledPlayersUUID.contains(player.getUniqueId());
    }

    public boolean isArmorVisible(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return true;
        if (player.isInvisible()) return !invisibleAlwaysHideGear;
        if (combatUntil.containsKey(player.getUniqueId())) return true;
        return !isEnabled(player);
    }

    public Player getByEntityId(int entityId) {
        return playersByEntityId.get(entityId);
    }

    public void track(Player player) {
        playersByEntityId.put(player.getEntityId(), player);
    }

    public void untrack(Player player) {
        playersByEntityId.remove(player.getEntityId());
        combatUntil.remove(player.getUniqueId());
    }

    public void markCombat(Player player) {
        if (combatRevealMillis <= 0 || !isEnabled(player)) return;
        boolean wasHidden = !isArmorVisible(player);
        combatUntil.put(player.getUniqueId(), System.currentTimeMillis() + combatRevealMillis);
        if (wasHidden) {
            messageHandler.sendActionBar(player, "%armor-revealed-combat%", Map.of());
            armorUpdater.updatePlayer(player);
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        combatUntil.entrySet().removeIf(entry -> {
            if (entry.getValue() > now) return false;
            Player player = Bukkit.getPlayer(entry.getKey());
            // deferred so the update sees the entry already removed
            if (player != null) Bukkit.getScheduler().runTask(plugin, () -> armorUpdater.updatePlayer(player));
            return true;
        });
        if (dirty) saveCurrentEnabledPlayers();
    }

    public void saveCurrentEnabledPlayers() {
        dirty = false;
        enabledPlayersConfig.set("enabled-players", enabledPlayersUUID.stream().map(UUID::toString).toList());
        try {
            enabledPlayersConfig.save(enabledPlayersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save enabled players", e);
        }
    }

    private void loadEnabledPlayers() {
        enabledPlayersFile = new File(plugin.getDataFolder(), "enabled-players.yml");
        if (!enabledPlayersFile.exists()) {
            plugin.saveResource("enabled-players.yml", false);
        }

        enabledPlayersConfig = YamlConfiguration.loadConfiguration(enabledPlayersFile);
        enabledPlayersUUID.addAll(enabledPlayersConfig.getStringList("enabled-players").stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet()));
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.invisibleAlwaysHideGear = config.getBoolean("invisibility-potion.always-hide-gear");
        this.combatRevealMillis = config.getLong("combat-reveal-seconds", 10) * 1000L;
    }
}
