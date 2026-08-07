package com.incogdev.homes.logging;

import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.Home;
import com.incogdev.homes.data.HomeManager;
import com.incogdev.homes.util.LocationUtil;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles both required log outputs:
 *  - the actions log (sethome / forcesethome / delhome / home / revoke),
 *    formatted per the "[Incog-Homes] =|= ..." spec and always echoed to
 *    console as well.
 *  - the homelist log, a standing snapshot of every home currently on the
 *    server, rewritten whenever a home is created, overwritten, deleted,
 *    or revoked.
 */
public class HomeLogger {

    private final Plugin plugin;
    private final HomesConfig config;

    public HomeLogger(Plugin plugin, HomesConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    private String timestamp() {
        return Instant.now().atZone(ZoneId.systemDefault()).format(config.getTimestampFormat());
    }

    private void writeAction(String line) {
        // Requirement: every action log message is also sent to console.
        plugin.getLogger().info(line);

        if (!config.isWriteToFiles()) {
            return;
        }
        appendLine(new File(plugin.getDataFolder(), config.getActionsLogFile()), line);
    }

    private void appendLine(File file, String line) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            Files.writeString(
                    file.toPath(),
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not write to " + file.getName(), ex);
        }
    }

    public void logSetHome(String playerName, UUID uuid, Home home, boolean forced) {
        String action = forced ? "Force-set a home" : "Set a home";
        String line = String.format(
                "[Incog-Homes] =|= '%s' (%s) | %s %s (~%s~) <|> %s",
                playerName, uuid, action, home.getName(),
                LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ()),
                timestamp()
        );
        writeAction(line);
    }

    public void logTeleport(String playerName, UUID uuid, Home home) {
        String line = String.format(
                "[Incog-Homes] =|= '%s' {%s} | Teleported to %s (~%s~) <|> %s",
                playerName, uuid, home.getName(),
                LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ()),
                timestamp()
        );
        writeAction(line);
    }

    public void logDeleteHome(String playerName, UUID uuid, Home home) {
        String line = String.format(
                "[Incog-Homes] =|= '%s' (%s) | Deleted a home %s (~%s~) <|> %s",
                playerName, uuid, home.getName(),
                LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ()),
                timestamp()
        );
        writeAction(line);
    }

    public void logRevoke(String targetName, UUID targetUuid, Home home, String adminName) {
        String line = String.format(
                "[Incog-Homes] =|= '%s' (%s) | Home %s (~%s~) was revoked by admin '%s' <|> %s",
                targetName, targetUuid, home.getName(),
                LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ()),
                adminName, timestamp()
        );
        writeAction(line);
    }

    /**
     * Rewrites the homelist log from scratch based on current state, so
     * deleted/revoked homes drop out and new/overwritten ones show up
     * with their latest data.
     */
    public void regenerateHomelist(HomeManager homeManager, Iterable<UUID> knownUuids) {
        if (!config.isWriteToFiles()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (UUID uuid : knownUuids) {
            String playerName = homeManager.getLastKnownName(uuid);
            if (playerName == null) {
                playerName = uuid.toString();
            }
            Map<String, Home> playerHomes = homeManager.getHomes(uuid);
            for (Home home : playerHomes.values()) {
                sb.append(String.format(
                        "'%s' (%s) =|= %s <|> %s /|\\ %s",
                        playerName, uuid, home.getName(),
                        LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ()),
                        Instant.ofEpochMilli(home.getCreatedTimestamp())
                                .atZone(ZoneId.systemDefault())
                                .format(config.getTimestampFormat())
                )).append(System.lineSeparator());
            }
        }

        File file = new File(plugin.getDataFolder(), config.getHomelistLogFile());
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not write to " + file.getName(), ex);
        }
    }
}
