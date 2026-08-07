package com.incogdev.homes.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin typed wrapper around config.yml so the rest of the plugin never
 * touches raw ConfigurationSection calls.
 */
public class HomesConfig {

    private final Plugin plugin;

    private String defaultHomeName;
    private int cooldownSeconds;
    private int defaultLimit;
    private final Map<String, Integer> permissionLimits = new LinkedHashMap<>();
    private boolean confirmOverwrite;
    private int confirmOverwriteSeconds;
    private int teleportWarmupSeconds;
    private boolean cancelWarmupOnMove;
    private boolean cancelWarmupOnDamage;
    private java.util.List<String> disabledWorlds;

    private boolean dangerChecksEnabled;
    private boolean blockLavaAndFire;
    private int fallCheckDistance;
    private int hostileMobRadius;
    private int combatTagSeconds;

    private DateTimeFormatter timestampFormat;
    private boolean writeToFiles;
    private String actionsLogFile;
    private String homelistLogFile;

    public HomesConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        var cfg = plugin.getConfig();

        defaultHomeName = cfg.getString("home.default-home-name", "home");
        cooldownSeconds = cfg.getInt("home.cooldown-seconds", 60);
        defaultLimit = cfg.getInt("home.default-limit", 3);
        confirmOverwrite = cfg.getBoolean("home.confirm-overwrite", true);
        confirmOverwriteSeconds = cfg.getInt("home.confirm-overwrite-seconds", 15);
        teleportWarmupSeconds = cfg.getInt("home.teleport-warmup-seconds", 3);
        cancelWarmupOnMove = cfg.getBoolean("home.cancel-warmup-on-move", true);
        cancelWarmupOnDamage = cfg.getBoolean("home.cancel-warmup-on-damage", true);
        disabledWorlds = cfg.getStringList("home.disabled-worlds");

        permissionLimits.clear();
        ConfigurationSection limitsSection = cfg.getConfigurationSection("home.permission-limits");
        if (limitsSection != null) {
            for (String perm : limitsSection.getKeys(false)) {
                permissionLimits.put(perm, limitsSection.getInt(perm));
            }
        }

        dangerChecksEnabled = cfg.getBoolean("danger-checks.enabled", true);
        blockLavaAndFire = cfg.getBoolean("danger-checks.block-lava-and-fire", true);
        fallCheckDistance = cfg.getInt("danger-checks.fall-check-distance", 5);
        hostileMobRadius = cfg.getInt("danger-checks.hostile-mob-radius", 6);
        combatTagSeconds = cfg.getInt("danger-checks.combat-tag-seconds", 10);

        String pattern = cfg.getString("logging.timestamp-format", "yyyy-MM-dd HH:mm:ss");
        timestampFormat = DateTimeFormatter.ofPattern(pattern);
        writeToFiles = cfg.getBoolean("logging.write-to-files", true);
        actionsLogFile = cfg.getString("logging.actions-log-file", "logs/actions.log");
        homelistLogFile = cfg.getString("logging.homelist-log-file", "logs/homelist.log");
    }

    public String getDefaultHomeName() {
        return defaultHomeName;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * The highest home limit the player qualifies for, taking
     * permission-limits into account. incoghomes.bypass.limit is checked
     * by the caller, not here.
     */
    public int getLimitFor(Player player) {
        int limit = defaultLimit;
        for (Map.Entry<String, Integer> entry : permissionLimits.entrySet()) {
            if (player.hasPermission(entry.getKey()) && entry.getValue() > limit) {
                limit = entry.getValue();
            }
        }
        return limit;
    }

    public boolean isConfirmOverwrite() {
        return confirmOverwrite;
    }

    public int getConfirmOverwriteSeconds() {
        return confirmOverwriteSeconds;
    }

    public int getTeleportWarmupSeconds() {
        return teleportWarmupSeconds;
    }

    public boolean isCancelWarmupOnMove() {
        return cancelWarmupOnMove;
    }

    public boolean isCancelWarmupOnDamage() {
        return cancelWarmupOnDamage;
    }

    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds != null && disabledWorlds.contains(worldName);
    }

    public boolean isDangerChecksEnabled() {
        return dangerChecksEnabled;
    }

    public boolean isBlockLavaAndFire() {
        return blockLavaAndFire;
    }

    public int getFallCheckDistance() {
        return fallCheckDistance;
    }

    public int getHostileMobRadius() {
        return hostileMobRadius;
    }

    public int getCombatTagSeconds() {
        return combatTagSeconds;
    }

    public DateTimeFormatter getTimestampFormat() {
        return timestampFormat;
    }

    public boolean isWriteToFiles() {
        return writeToFiles;
    }

    public String getActionsLogFile() {
        return actionsLogFile;
    }

    public String getHomelistLogFile() {
        return homelistLogFile;
    }

    public String getMessage(String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String message = plugin.getConfig().getString("messages." + key, "");
        return prefix + message;
    }
}
