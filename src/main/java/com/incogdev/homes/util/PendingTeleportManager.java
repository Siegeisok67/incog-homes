package com.incogdev.homes.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks in-progress /home teleport warmups so they can be cancelled if
 * the player moves or takes damage before the countdown finishes.
 */
public class PendingTeleportManager {

    private static final double MOVE_THRESHOLD_SQUARED = 0.09; // ~0.3 blocks

    private record Pending(BukkitTask task, Location startLocation, boolean cancelOnMove, boolean cancelOnDamage) {
    }

    private final Plugin plugin;
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public PendingTeleportManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPending(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public void start(Player player, int warmupSeconds, boolean cancelOnMove, boolean cancelOnDamage,
                       Runnable onComplete, Runnable onCancelledByMove, Runnable onCancelledByDamage) {
        UUID uuid = player.getUniqueId();
        Location startLocation = player.getLocation().clone();

        BukkitTask task = new BukkitRunnable() {
            int ticksRemaining = warmupSeconds * 20;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pending.remove(uuid);
                    cancel();
                    return;
                }

                Pending current = pending.get(uuid);
                if (current == null) {
                    // Removed externally (e.g. damage cancel) — stop silently.
                    cancel();
                    return;
                }

                if (current.cancelOnMove() && hasMoved(player, current.startLocation())) {
                    pending.remove(uuid);
                    cancel();
                    onCancelledByMove.run();
                    return;
                }

                ticksRemaining -= 4;
                if (ticksRemaining <= 0) {
                    pending.remove(uuid);
                    cancel();
                    onComplete.run();
                }
            }
        }.runTaskTimer(plugin, 4L, 4L);

        pending.put(uuid, new Pending(task, startLocation, cancelOnMove, cancelOnDamage));
    }

    /**
     * Called by the damage listener. Cancels the pending teleport if one
     * exists and cancel-on-damage is enabled for it.
     */
    public void cancelForDamage(UUID uuid, Runnable onCancelledByDamage) {
        Pending current = pending.remove(uuid);
        if (current != null && current.cancelOnDamage()) {
            current.task().cancel();
            onCancelledByDamage.run();
        } else if (current != null) {
            // Damage cancel disabled — put it back.
            pending.put(uuid, current);
        }
    }

    private boolean hasMoved(Player player, Location startLocation) {
        Location now = player.getLocation();
        if (now.getWorld() == null || startLocation.getWorld() == null
                || !now.getWorld().equals(startLocation.getWorld())) {
            return true;
        }
        return now.distanceSquared(startLocation) > MOVE_THRESHOLD_SQUARED;
    }
}
