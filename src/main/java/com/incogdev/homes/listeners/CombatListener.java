package com.incogdev.homes.listeners;

import com.incogdev.homes.util.CombatTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    private final CombatTracker combatTracker;

    public CombatListener(CombatTracker combatTracker) {
        this.combatTracker = combatTracker;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            combatTracker.markDamaged(player.getUniqueId());
        }
    }
}
