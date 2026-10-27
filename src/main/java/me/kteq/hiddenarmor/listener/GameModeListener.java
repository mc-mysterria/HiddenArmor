package me.kteq.hiddenarmor.listener;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class GameModeListener implements Listener {

    private final HiddenArmor plugin;
    private final PlayerManager hiddenArmorManager;

    public GameModeListener(HiddenArmor plugin) {
        this.plugin = plugin;
        this.hiddenArmorManager = plugin.getPlayerManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!hiddenArmorManager.isEnabled(player) && !player.isInvisible()) return;

        // creative clients write their inventory back to the server, so they must never hold the stripped copies
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            plugin.getArmorUpdater().revealSelf(player);
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getArmorUpdater().updatePlayer(player));
    }
}
