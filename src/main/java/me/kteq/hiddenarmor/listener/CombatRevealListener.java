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

    // Players must be able to read an opponent's armour tier in PvP.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player attacker) || attacker.equals(victim)) return;

        playerManager.markCombat(victim);
        playerManager.markCombat(attacker);
    }
}
