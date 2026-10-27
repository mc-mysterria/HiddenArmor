package me.kteq.hiddenarmor.listener;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatRevealListener implements Listener {

    private final PlayerManager playerManager;

    public CombatRevealListener(HiddenArmor plugin) {
        this.playerManager = plugin.getPlayerManager();
    }

    // Players must be able to read an opponent's armour tier in PvP; mobs never trigger this.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || isNpc(victim)) return;

        // causing entity also resolves TNT, lingering potion clouds and other indirect sources to the player behind them
        if (!(event.getDamageSource().getCausingEntity() instanceof Player attacker) || isNpc(attacker) || attacker.equals(victim)) return;

        playerManager.markCombat(victim);
        playerManager.markCombat(attacker);
    }

    private boolean isNpc(Player player) {
        return player.hasMetadata("NPC");
    }
}
