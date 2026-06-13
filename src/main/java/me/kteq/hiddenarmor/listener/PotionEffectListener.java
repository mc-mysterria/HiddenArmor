package me.kteq.hiddenarmor.listener;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorUpdateHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PotionEffectListener implements Listener {

    HiddenArmor plugin;

    PlayerManager hiddenArmorManager;
    ArmorUpdateHandler armorUpdater;

    public PotionEffectListener(HiddenArmor plugin) {
        this.plugin = plugin;
        this.hiddenArmorManager = plugin.getPlayerManager();
        this.armorUpdater = plugin.getArmorUpdater();
    }

    @EventHandler
    public void onPlayerInvisibleEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PotionEffectType newType = event.getNewEffect() != null ? event.getNewEffect().getType() : null;
        PotionEffectType oldType = event.getOldEffect() != null ? event.getOldEffect().getType() : null;
        if (!PotionEffectType.INVISIBILITY.equals(newType) && !PotionEffectType.INVISIBILITY.equals(oldType)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                armorUpdater.updatePlayer(player);
            }
        }.runTaskLater(plugin, 2L);
    }
}
