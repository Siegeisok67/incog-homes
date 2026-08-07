package com.incogdev.homes.commands;

import com.incogdev.homes.IncogHomes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeNameTabCompleter implements TabCompleter {

    private final IncogHomes plugin;

    public HomeNameTabCompleter(IncogHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return Collections.emptyList();
        }
        String partial = args[0].toLowerCase();
        return plugin.getHomeManager().getHomes(player.getUniqueId()).values().stream()
                .map(home -> home.getName())
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}
