package com.incogdev.homes.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A single stored home location.
 */
public class Home {

    private final String name;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long createdTimestamp;

    public Home(String name, String worldName, double x, double y, double z,
                float yaw, float pitch, long createdTimestamp) {
        this.name = name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdTimestamp = createdTimestamp;
    }

    public static Home fromLocation(String name, Location location, long createdTimestamp) {
        World world = location.getWorld();
        return new Home(
                name,
                world == null ? "world" : world.getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                createdTimestamp
        );
    }

    /**
     * Rebuilds an in-game Location from this home. Returns null if the
     * world isn't currently loaded.
     */
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }
}
