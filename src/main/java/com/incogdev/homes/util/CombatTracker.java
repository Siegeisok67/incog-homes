package com.incogdev.homes.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTracker {

    private final Map<UUID, Long> lastDamaged = new HashMap<>();

    public void markDamaged(UUID uuid) {
        lastDamaged.put(uuid, System.currentTimeMillis());
    }

    public boolean isRecentlyInCombat(UUID uuid, int combatTagSeconds) {
        if (combatTagSeconds <= 0) {
            return false;
        }
        Long last = lastDamaged.get(uuid);
        if (last == null) {
            return false;
        }
        return (System.currentTimeMillis() - last) < (combatTagSeconds * 1000L);
    }
}
