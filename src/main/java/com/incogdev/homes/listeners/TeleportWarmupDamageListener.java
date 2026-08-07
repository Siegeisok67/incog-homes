package com.incogdev.homes.listeners;

import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.util.MessageUtil;
import com.incogdev.homes.util.PendingTeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class TeleportWarmupDamageListener implements Listener {

    private final PendingTeleportManager pendingTeleportManager;
    private final HomesConfig config;

    public TeleportWarmupDamageListener(PendingTeleportManager pendingTeleportManager, HomesConfig config) {
        this.pendingTeleportManager = pendingTeleportManager;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!pendingTeleportManager.isPending(player.getUniqueId())) {
            return;
        }
        pendingTeleportManager.cancelForDamage(player.getUniqueId(), () ->
                MessageUtil.send(player, config.getMessage("home-teleport-cancelled-damage")));
    }
}
