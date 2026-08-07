package com.incogdev.homes.util;

import com.incogdev.homes.config.HomesConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class DangerChecker {

    private final HomesConfig config;
    private final CombatTracker combatTracker;

    public DangerChecker(HomesConfig config, CombatTracker combatTracker) {
        this.config = config;
        this.combatTracker = combatTracker;
    }

    public DangerResult check(Player player) {
        if (!config.isDangerChecksEnabled() || player.hasPermission("incoghomes.bypass.danger")) {
            return DangerResult.safe();
        }

        if (config.isBlockLavaAndFire()) {
            Material feetBlock = player.getLocation().getBlock().getType();
            Material belowFeet = player.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
            if (player.getFireTicks() > 0 || isLava(feetBlock) || isLava(belowFeet)) {
                return DangerResult.unsafe(DangerResult.Reason.LAVA_OR_FIRE);
            }
        }

        int fallDistance = config.getFallCheckDistance();
        if (fallDistance > 0 && !hasGroundNearby(player.getLocation(), fallDistance)) {
            return DangerResult.unsafe(DangerResult.Reason.NO_GROUND);
        }

        int mobRadius = config.getHostileMobRadius();
        if (mobRadius > 0 && hasHostileMobNearby(player, mobRadius)) {
            return DangerResult.unsafe(DangerResult.Reason.HOSTILE_MOB_NEARBY);
        }

        if (combatTracker.isRecentlyInCombat(player.getUniqueId(), config.getCombatTagSeconds())) {
            return DangerResult.unsafe(DangerResult.Reason.RECENT_COMBAT);
        }

        return DangerResult.safe();
    }

    private boolean isLava(Material material) {
        return material == Material.LAVA;
    }

    private boolean hasGroundNearby(Location location, int distance) {
        Block current = location.getBlock();
        for (int i = 0; i <= distance; i++) {
            Block check = current.getRelative(0, -i, 0);
            if (check.getType().isSolid()) {
                return true;
            }
            if (check.getY() <= check.getWorld().getMinHeight()) {
                break;
            }
        }
        return false;
    }

    private boolean hasHostileMobNearby(Player player, int radius) {
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Monster) {
                return true;
            }
        }
        return false;
    }
}
