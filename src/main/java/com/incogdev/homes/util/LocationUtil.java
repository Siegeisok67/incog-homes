package com.incogdev.homes.util;

import org.bukkit.Location;

public final class LocationUtil {

    private LocationUtil() {
    }

    /**
     * Compact "world:x,y,z" form used in log files and admin messages,
     * e.g. "world_nether:123,64,-45".
     */
    public static String format(Location location) {
        String world = location.getWorld() != null ? location.getWorld().getName() : "unknown";
        return world + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static String format(String worldName, double x, double y, double z) {
        return worldName + ":" + Math.round(x) + "," + Math.round(y) + "," + Math.round(z);
    }
}
