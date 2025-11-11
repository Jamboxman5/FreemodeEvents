package net.jahcraft.freemodeevents.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {

	public Economy eco;
	public static Main plugin;

    private BukkitRunnable currentEvent;
	
	@Override
	public void onEnable() {
		
		if (!setupEconomy()) {
			
			Bukkit.getLogger().info("Economy not detected! Disabling FreemodeEvents!");
			getServer().getPluginManager().disablePlugin(this);
			return;
			
		}

        plugin = this;
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	private boolean setupEconomy() {
		
		RegisteredServiceProvider<Economy> economy = getServer().
				getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		
		if (economy != null)
			eco = economy.getProvider();
		return (eco != null);
		
	}

    public boolean canRunEvent() {
        return (currentEvent == null);
    }

    public void runEvent(BukkitRunnable event) {
        if (!canRunEvent()) {
            Bukkit.getLogger().warning("Tried to run event while event running!");
            return;
        }
        currentEvent = event;
        currentEvent.runTaskAsynchronously(this);
    }
	
}
