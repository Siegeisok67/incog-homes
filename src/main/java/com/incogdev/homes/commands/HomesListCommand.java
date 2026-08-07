package com.incogdev.homes.commands;

import com.incogdev.homes.IncogHomes;
import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.Home;
import com.incogdev.homes.util.LocationUtil;
import com.incogdev.homes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

public class HomesListCommand implements CommandExecutor {

    private final IncogHomes plugin;

    public HomesListCommand(IncogHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        HomesConfig config = plugin.getHomesConfig();
        Map<String, Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());

        if (homes.isEmpty()) {
            MessageUtil.send(player, config.getMessage("home-no-homes"));
            return true;
        }

        MessageUtil.send(player, "&8[&bIncog-Homes&8] &7Your homes (&f" + homes.size() + "&7/&f"
                + config.getLimitFor(player) + "&7):");
        for (Home home : homes.values()) {
            String location = LocationUtil.format(home.getWorldName(), home.getX(), home.getY(), home.getZ());
            String timestamp = Instant.ofEpochMilli(home.getCreatedTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .format(config.getTimestampFormat());
            MessageUtil.send(player, "&7 - &f" + home.getName() + "&7: &f" + location + " &7(set " + timestamp + ")");
        }
        return true;
    }
}
