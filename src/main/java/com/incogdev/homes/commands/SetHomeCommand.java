package com.incogdev.homes.commands;

import com.incogdev.homes.IncogHomes;
import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.Home;
import com.incogdev.homes.util.DangerResult;
import com.incogdev.homes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SetHomeCommand implements CommandExecutor {

    // Shared between /sethome and /forcesethome so a confirm on one is
    // honored no matter which command triggers the second run.
    private static final Map<String, Long> PENDING_OVERWRITE = new ConcurrentHashMap<>();

    private final IncogHomes plugin;
    private final boolean forced;

    public SetHomeCommand(IncogHomes plugin, boolean forced) {
        this.plugin = plugin;
        this.forced = forced;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        HomesConfig config = plugin.getHomesConfig();
        String name = args.length > 0 ? args[0] : config.getDefaultHomeName();

        if (config.isWorldDisabled(player.getWorld().getName())) {
            MessageUtil.send(player, config.getMessage("world-disabled"));
            return true;
        }

        boolean exists = plugin.getHomeManager().hasHome(player.getUniqueId(), name);
        boolean atLimit = !exists
                && !player.hasPermission("incoghomes.bypass.limit")
                && plugin.getHomeManager().getHomeCount(player.getUniqueId()) >= config.getLimitFor(player);

        if (atLimit) {
            MessageUtil.send(player, config.getMessage("home-limit-reached"),
                    Map.of("limit", String.valueOf(config.getLimitFor(player))));
            return true;
        }

        if (!forced) {
            DangerResult result = plugin.getDangerChecker().check(player);
            if (!result.isSafe()) {
                sendDangerMessage(player, result.getReason());
                return true;
            }
        }

        if (exists && config.isConfirmOverwrite()) {
            String key = player.getUniqueId() + ":" + name.toLowerCase();
            Long pendingSince = PENDING_OVERWRITE.get(key);
            long now = System.currentTimeMillis();
            boolean confirmedInTime = pendingSince != null
                    && (now - pendingSince) <= config.getConfirmOverwriteSeconds() * 1000L;

            if (!confirmedInTime) {
                PENDING_OVERWRITE.put(key, now);
                MessageUtil.send(player, config.getMessage("confirm-overwrite"), Map.of(
                        "name", name,
                        "seconds", String.valueOf(config.getConfirmOverwriteSeconds())
                ));
                return true;
            }
            PENDING_OVERWRITE.remove(key);
        }

        Home home = plugin.getHomeManager().setHome(player.getUniqueId(), player.getName(), name, player.getLocation());
        plugin.getHomeLogger().logSetHome(player.getName(), player.getUniqueId(), home, forced);
        plugin.getHomeLogger().regenerateHomelist(plugin.getHomeManager(), plugin.getHomeManager().getKnownUuids());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        MessageUtil.send(player, config.getMessage(forced ? "home-force-set" : "home-set"), placeholders);
        return true;
    }

    private void sendDangerMessage(Player player, DangerResult.Reason reason) {
        HomesConfig config = plugin.getHomesConfig();
        String key = switch (reason) {
            case LAVA_OR_FIRE -> "danger-lava-fire";
            case NO_GROUND -> "danger-fall";
            case HOSTILE_MOB_NEARBY -> "danger-mobs";
            case RECENT_COMBAT -> "danger-combat";
            default -> null;
        };
        if (key != null) {
            MessageUtil.send(player, config.getMessage(key));
        }
    }
}
