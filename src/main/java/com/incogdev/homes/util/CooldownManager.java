package com.incogdev.homes.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> lastUse = new HashMap<>();

    /**
     * Seconds remaining before the player can use /home again, or 0 if
     * they're free to use it now.
     */
    public long getRemainingSeconds(UUID uuid, int cooldownSeconds) {
        Long last = lastUse.get(uuid);
        if (last == null) {
            return 0;
        }
        long elapsedMillis = System.currentTimeMillis() - last;
        long remainingMillis = (cooldownSeconds * 1000L) - elapsedMillis;
        if (remainingMillis <= 0) {
            return 0;
        }
        return (remainingMillis + 999) / 1000; // round up
    }

    public void markUsed(UUID uuid) {
        lastUse.put(uuid, System.currentTimeMillis());
    }
}
