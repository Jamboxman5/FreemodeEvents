package net.jahcraft.freemodeevents.main;

import net.jahcraft.freemodeevents.events.EventController;
import net.jahcraft.freemodeevents.events.FreemodeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	public static Economy eco;
	public static Main plugin;

    private FreemodeEvent currentEvent;

    private final int eventCooldown = 60;
    private EventController controller;

    private long lastEventEnd = 0;
	
	@Override
	public void onEnable() {
		
		if (!setupEconomy()) {
			
			Bukkit.getLogger().info("Economy not detected! Disabling FreemodeEvents!");
			getServer().getPluginManager().disablePlugin(this);
			return;
			
		}

        plugin = this;

        controller = new EventController(eventCooldown);
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
        return (currentEvent == null && (System.currentTimeMillis() - lastEventEnd >= (eventCooldown * 1000)));
    }

    public void runEvent(FreemodeEvent event) {
        if (!canRunEvent()) {
            Bukkit.getLogger().warning("Tried to run event while event running!");
            return;
        }
        currentEvent = event;
        currentEvent.runTaskAsynchronously(this);
    }

    public void finishEvent(FreemodeEvent event) {
        if (currentEvent == event) {
            currentEvent = null;
            lastEventEnd = System.currentTimeMillis();
        }
        HandlerList.unregisterAll(event);

    }

    public boolean isRunningEvent(FreemodeEvent event) {
        return (currentEvent==event);
    }

    public FreemodeEvent getRunningEvent() { return currentEvent; }
}
