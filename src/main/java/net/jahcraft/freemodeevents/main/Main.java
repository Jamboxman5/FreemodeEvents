package net.jahcraft.freemodeevents.main;

import net.jahcraft.freemodeevents.chat.UnscrambleEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {

	public Economy eco;
	public static Main plugin;

    private BukkitRunnable currentEvent;

    private final int eventInterval = 60;
    private EventController controller;
	
	@Override
	public void onEnable() {
		
		if (!setupEconomy()) {
			
			Bukkit.getLogger().info("Economy not detected! Disabling FreemodeEvents!");
			getServer().getPluginManager().disablePlugin(this);
			return;
			
		}

        plugin = this;

        controller = new EventController(eventInterval);
        controller.runTaskAsynchronously(this);

		
	}
	
	@Override
	public void onDisable() {

        controller.stop();
        controller.cancel();

		if (currentEvent != null) {
            currentEvent.cancel();
        }
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

    public void finishEvent(BukkitRunnable event) {
        if (currentEvent == event) currentEvent = null;
    }

    public boolean isRunningEvent(BukkitRunnable event) {
        return (currentEvent==event);
    }

    public BukkitRunnable getRunningEvent() { return currentEvent; }
}
