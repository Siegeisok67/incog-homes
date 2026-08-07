package com.incogdev.homes.commands;

import com.incogdev.homes.IncogHomes;
import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.Home;
import com.incogdev.homes.util.LocationUtil;
import com.incogdev.homes.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

public class HomeAdminCommand implements CommandExecutor {

    private final IncogHomes plugin;

    public HomeAdminCommand(IncogHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HomesConfig config = plugin.getHomesConfig();

        if (args.length < 1) {
            MessageUtil.send(sender, "&cUsage: /homeadmin <revoke|list> ...");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "revoke" -> handleRevoke(sender, config, args);
            case "list" -> handleList(sender, config, args);
            default -> MessageUtil.send(sender, "&cUsage: /homeadmin <revoke|list> ...");
        }
        return true;
    }

    private void handleRevoke(CommandSender sender, HomesConfig config, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, config.getMessage("admin-revoke-usage"));
            return;
        }

        String targetName = args[1];
        String homeName = args[2];
        UUID targetUuid = resolveUuid(targetName);

        Home home = plugin.getHomeManager().getHome(targetUuid, homeName);
        if (home == null) {
            MessageUtil.send(sender, config.getMessage("admin-home-not-found"),
                    Map.of("player", targetName, "name", homeName));
            return;
        }

        // Tell the admin exactly where the home is before it's removed.
        String location = LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ());
        MessageUtil.send(sender, config.getMessage("admin-revoke-location"), Map.of(
                "name", homeName, "player", targetName, "location", location
        ));

        plugin.getHomeManager().deleteHome(targetUuid, homeName);
        plugin.getHomeLogger().logRevoke(targetName, targetUuid, home, sender.getName());
        plugin.getHomeLogger().regenerateHomelist(plugin.getHomeManager(), plugin.getHomeManager().getKnownUuids());

        MessageUtil.send(sender, config.getMessage("admin-revoke-confirmed"),
                Map.of("name", homeName, "player", targetName));
    }

    private void handleList(CommandSender sender, HomesConfig config, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(sender, config.getMessage("admin-list-usage"));
            return;
        }

        String targetName = args[1];
        UUID targetUuid = resolveUuid(targetName);
        Map<String, Home> homes = plugin.getHomeManager().getHomes(targetUuid);

        if (homes.isEmpty()) {
            MessageUtil.send(sender, config.getMessage("admin-player-no-homes"), Map.of("player", targetName));
            return;
        }

        MessageUtil.send(sender, config.getMessage("admin-list-header"), Map.of("player", targetName));
        for (Home home : homes.values()) {
            String location = LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ());
            String timestamp = Instant.ofEpochMilli(home.getCreatedTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .format(config.getTimestampFormat());
            MessageUtil.send(sender, config.getMessage("admin-list-entry"), Map.of(
                    "name", home.getName(), "location", location, "timestamp", timestamp
            ));
        }
    }

    private UUID resolveUuid(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        return offlinePlayer.getUniqueId();
    }
}
