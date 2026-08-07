package com.incogdev.homes.commands;

import com.incogdev.homes.IncogHomes;
import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.Home;
import com.incogdev.homes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class DelHomeCommand implements CommandExecutor {

    private final IncogHomes plugin;

    public DelHomeCommand(IncogHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        HomesConfig config = plugin.getHomesConfig();
        if (args.length < 1) {
            MessageUtil.send(player, "&cUsage: /delhome <name>");
            return true;
        }

        String name = args[0];
        Home home = plugin.getHomeManager().deleteHome(player.getUniqueId(), name);
        if (home == null) {
            MessageUtil.send(player, config.getMessage("home-not-found"), Map.of("name", name));
            return true;
        }

        plugin.getHomeLogger().logDeleteHome(player.getName(), player.getUniqueId(), home);
        plugin.getHomeLogger().regenerateHomelist(plugin.getHomeManager(), plugin.getHomeManager().getKnownUuids());

        MessageUtil.send(player, config.getMessage("home-deleted"), Map.of("name", name));
        return true;
    }
}
