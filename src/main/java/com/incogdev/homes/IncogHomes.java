package com.incogdev.homes;

import com.incogdev.homes.commands.DelHomeCommand;
import com.incogdev.homes.commands.HomeAdminCommand;
import com.incogdev.homes.commands.HomeCommand;
import com.incogdev.homes.commands.HomeNameTabCompleter;
import com.incogdev.homes.commands.HomesListCommand;
import com.incogdev.homes.commands.SetHomeCommand;
import com.incogdev.homes.config.HomesConfig;
import com.incogdev.homes.data.HomeManager;
import com.incogdev.homes.listeners.CombatListener;
import com.incogdev.homes.listeners.TeleportWarmupDamageListener;
import com.incogdev.homes.logging.HomeLogger;
import com.incogdev.homes.util.CombatTracker;
import com.incogdev.homes.util.CooldownManager;
import com.incogdev.homes.util.DangerChecker;
import com.incogdev.homes.util.PendingTeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

public class IncogHomes extends JavaPlugin {

    private HomesConfig homesConfig;
    private HomeManager homeManager;
    private HomeLogger homeLogger;
    private DangerChecker dangerChecker;
    private CooldownManager cooldownManager;
    private CombatTracker combatTracker;
    private PendingTeleportManager pendingTeleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.homesConfig = new HomesConfig(this);
        this.homesConfig.reload();

        this.homeManager = new HomeManager(getDataFolder(), getLogger());
        this.homeManager.load();

        this.homeLogger = new HomeLogger(this, homesConfig);
        this.combatTracker = new CombatTracker();
        this.dangerChecker = new DangerChecker(homesConfig, combatTracker);
        this.cooldownManager = new CooldownManager();
        this.pendingTeleportManager = new PendingTeleportManager(this);

        getServer().getPluginManager().registerEvents(new CombatListener(combatTracker), this);
        getServer().getPluginManager().registerEvents(
                new TeleportWarmupDamageListener(pendingTeleportManager, homesConfig), this);

        SetHomeCommand setHomeCommand = new SetHomeCommand(this, false);
        SetHomeCommand forceSetHomeCommand = new SetHomeCommand(this, true);
        HomeCommand homeCommand = new HomeCommand(this);
        DelHomeCommand delHomeCommand = new DelHomeCommand(this);
        HomesListCommand homesListCommand = new HomesListCommand(this);
        HomeAdminCommand homeAdminCommand = new HomeAdminCommand(this);

        getCommand("sethome").setExecutor(setHomeCommand);
        getCommand("forcesethome").setExecutor(forceSetHomeCommand);
        getCommand("home").setExecutor(homeCommand);
        getCommand("home").setTabCompleter(new HomeNameTabCompleter(this));
        getCommand("delhome").setExecutor(delHomeCommand);
        getCommand("delhome").setTabCompleter(new HomeNameTabCompleter(this));
        getCommand("homes").setExecutor(homesListCommand);
        getCommand("homeadmin").setExecutor(homeAdminCommand);

        getLogger().info("Incog-Homes enabled — targeting Purpur 26.2.");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.save();
        }
    }

    public void reload() {
        homesConfig.reload();
    }

    public HomesConfig getHomesConfig() {
        return homesConfig;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public HomeLogger getHomeLogger() {
        return homeLogger;
    }

    public DangerChecker getDangerChecker() {
        return dangerChecker;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CombatTracker getCombatTracker() {
        return combatTracker;
    }

    public PendingTeleportManager getPendingTeleportManager() {
        return pendingTeleportManager;
    }
}
