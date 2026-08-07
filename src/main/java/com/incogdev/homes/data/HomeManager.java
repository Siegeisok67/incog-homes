package com.incogdev.homes.data;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads, holds, and persists every player's homes in homes.yml.
 *
 * File layout:
 * players:
 *   <uuid>:
 *     name: <last known username>
 *     homes:
 *       <homename>:
 *         world: ...
 *         x/y/z/yaw/pitch: ...
 *         created: <epoch millis>
 */
public class HomeManager {

    private final File file;
    private final Logger logger;
    private YamlConfiguration yaml;

    // uuid -> (homename -> Home), case-preserving but looked up case-insensitively
    private final Map<UUID, Map<String, Home>> homes = new LinkedHashMap<>();
    // uuid -> last known username, kept so admin commands can resolve names offline
    private final Map<UUID, String> lastKnownNames = new LinkedHashMap<>();

    public HomeManager(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "homes.yml");
        this.logger = logger;
    }

    public void load() {
        if (!file.exists()) {
            yaml = new YamlConfiguration();
            return;
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        homes.clear();
        lastKnownNames.clear();

        ConfigurationSection playersSection = yaml.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (String uuidStr : playersSection.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ex) {
                continue;
            }

            ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidStr);
            if (playerSection == null) {
                continue;
            }

            lastKnownNames.put(uuid, playerSection.getString("name", uuidStr));

            Map<String, Home> playerHomes = new LinkedHashMap<>();
            ConfigurationSection homesSection = playerSection.getConfigurationSection("homes");
            if (homesSection != null) {
                for (String homeName : homesSection.getKeys(false)) {
                    ConfigurationSection h = homesSection.getConfigurationSection(homeName);
                    if (h == null) {
                        continue;
                    }
                    Home home = new Home(
                            homeName,
                            h.getString("world", "world"),
                            h.getDouble("x"),
                            h.getDouble("y"),
                            h.getDouble("z"),
                            (float) h.getDouble("yaw"),
                            (float) h.getDouble("pitch"),
                            h.getLong("created", System.currentTimeMillis())
                    );
                    playerHomes.put(homeName.toLowerCase(), home);
                }
            }
            homes.put(uuid, playerHomes);
        }
    }

    public synchronized void save() {
        if (yaml == null) {
            yaml = new YamlConfiguration();
        }
        yaml.set("players", null); // clear before rewrite

        for (Map.Entry<UUID, Map<String, Home>> entry : homes.entrySet()) {
            UUID uuid = entry.getKey();
            String base = "players." + uuid;
            yaml.set(base + ".name", lastKnownNames.getOrDefault(uuid, uuid.toString()));

            for (Home home : entry.getValue().values()) {
                String homeBase = base + ".homes." + home.getName();
                yaml.set(homeBase + ".world", home.getWorldName());
                yaml.set(homeBase + ".x", home.getX());
                yaml.set(homeBase + ".y", home.getY());
                yaml.set(homeBase + ".z", home.getZ());
                yaml.set(homeBase + ".yaw", home.getYaw());
                yaml.set(homeBase + ".pitch", home.getPitch());
                yaml.set(homeBase + ".created", home.getCreatedTimestamp());
            }
        }

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            yaml.save(file);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save homes.yml", ex);
        }
    }

    public void rememberName(UUID uuid, String username) {
        lastKnownNames.put(uuid, username);
    }

    public String getLastKnownName(UUID uuid) {
        return lastKnownNames.get(uuid);
    }

    public Map<String, Home> getHomes(UUID uuid) {
        return homes.getOrDefault(uuid, Map.of());
    }

    public Home getHome(UUID uuid, String name) {
        Map<String, Home> playerHomes = homes.get(uuid);
        if (playerHomes == null) {
            return null;
        }
        return playerHomes.get(name.toLowerCase());
    }

    public boolean hasHome(UUID uuid, String name) {
        return getHome(uuid, name) != null;
    }

    public int getHomeCount(UUID uuid) {
        return getHomes(uuid).size();
    }

    /**
     * Every UUID that has (or has ever had) an entry, for regenerating
     * the homelist log.
     */
    public java.util.Set<UUID> getKnownUuids() {
        return homes.keySet();
    }

    /**
     * Creates or overwrites a home. Returns the new Home.
     */
    public Home setHome(UUID uuid, String username, String name, Location location) {
        rememberName(uuid, username);
        Home home = Home.fromLocation(name, location, System.currentTimeMillis());
        homes.computeIfAbsent(uuid, k -> new LinkedHashMap<>()).put(name.toLowerCase(), home);
        save();
        return home;
    }

    /**
     * Removes a home. Returns the removed Home, or null if it didn't exist.
     */
    public Home deleteHome(UUID uuid, String name) {
        Map<String, Home> playerHomes = homes.get(uuid);
        if (playerHomes == null) {
            return null;
        }
        Home removed = playerHomes.remove(name.toLowerCase());
        if (removed != null) {
            save();
        }
        return removed;
    }
}
