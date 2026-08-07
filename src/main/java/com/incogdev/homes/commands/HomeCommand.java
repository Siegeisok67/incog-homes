package com.incogdev.homes.commands;

import com.incogdev.homes.IncogHomes;
import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.Home;
import com.incogdev.homes.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class HomeCommand implements CommandExecutor {

    private final IncogHomes plugin;

    public HomeCommand(IncogHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        HomesConfig config = plugin.getHomesConfig();
        String name = args.length > 0 ? args[0] : config.getDefaultHomeName();

        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), name);
        if (home == null) {
            if (plugin.getHomeManager().getHomeCount(player.getUniqueId()) == 0) {
                MessageUtil.send(player, config.getMessage("home-no-homes"));
            } else {
                MessageUtil.send(player, config.getMessage("home-not-found"), Map.of("name", name));
            }
            return true;
        }

        if (config.isWorldDisabled(player.getWorld().getName())) {
            MessageUtil.send(player, config.getMessage("world-disabled"));
            return true;
        }

        Location destination = home.toLocation();
        if (destination == null) {
            MessageUtil.send(player, config.getMessage("home-not-found"), Map.of("name", name));
            return true;
        }

        if (!player.hasPermission("incoghomes.bypass.cooldown")) {
            long remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), config.getCooldownSeconds());
            if (remaining > 0) {
                MessageUtil.send(player, config.getMessage("home-cooldown"), Map.of("seconds", String.valueOf(remaining)));
                return true;
            }
        }

        int warmup = config.getTeleportWarmupSeconds();
        if (warmup <= 0) {
            teleportNow(player, home, destination);
            return true;
        }

        MessageUtil.send(player, config.getMessage("home-teleporting"), Map.of(
                "name", home.getName(),
                "seconds", String.valueOf(warmup)
        ));

        plugin.getPendingTeleportManager().start(
                player,
                warmup,
                config.isCancelWarmupOnMove(),
                config.isCancelWarmupOnDamage(),
                () -> teleportNow(player, home, destination),
                () -> MessageUtil.send(player, config.getMessage("home-teleport-cancelled-move")),
                () -> MessageUtil.send(player, config.getMessage("home-teleport-cancelled-damage"))
        );
        return true;
    }

    private void teleportNow(Player player, Home home, Location destination) {
        player.teleport(destination);
        plugin.getCooldownManager().markUsed(player.getUniqueId());
        plugin.getHomeLogger().logTeleport(player.getName(), player.getUniqueId(), home);
        HomesConfig config = plugin.getHomesConfig();
        MessageUtil.send(player, config.getMessage("home-teleported"), Map.of("name", home.getName()));
    }
}
