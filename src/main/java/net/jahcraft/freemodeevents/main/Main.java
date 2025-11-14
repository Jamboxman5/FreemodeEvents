package net.jahcraft.freemodeevents.main;

import net.jahcraft.freemodeevents.commands.EventsCommand;
import net.jahcraft.freemodeevents.config.DataManager;
import net.jahcraft.freemodeevents.events.EventController;
import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.listeners.JoinListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.scoreboard.Scoreboard;

public class Main extends JavaPlugin {

	public static Economy eco;
	public static Main plugin;
    public static DataManager config;

    private FreemodeEvent currentEvent;
    private Scoreboard currentScoreboard;

    private int eventCooldown;
    private EventController controller;

    private long lastEventEnd = 0;
	
	@Override
	public void onEnable() {
		
		if (!setupEconomy()) {
			
			Bukkit.getLogger().info("Economy not detected! Disabling FreemodeEvents!");
			getServer().getPluginManager().disablePlugin(this);
			return;
			
		}

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        plugin = this;
        config = new DataManager();
        loadConfiguration();

        getCommand("events").setExecutor(new EventsCommand());

        controller = new EventController(1);
        controller.runTaskAsynchronously(this);

		
	}
	
	@Override
	public void onDisable() {

        //Clear scoreboards
        for (Player p : Bukkit.getOnlinePlayers()) {

            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        controller.stop();
        controller.cancel();

		if (currentEvent != null) {
            finishEvent(currentEvent);
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
//        Bukkit.getLogger().info("Event running? " + (currentEvent != null));
//        Bukkit.getLogger().info("Cooldown? " + eventCooldown + "s");
//        Bukkit.getLogger().info("Cooldown passed? " + ((System.currentTimeMillis() - lastEventEnd)/1000) + "s");
        return (currentEvent == null && !Bukkit.getOnlinePlayers().isEmpty() && (System.currentTimeMillis() - lastEventEnd >= (eventCooldown * 1000)));
    }

    public void runEvent(FreemodeEvent event) {
        if (!canRunEvent()) {
            Bukkit.getLogger().warning("Tried to run event while event running!");
            return;
        }
        getServer().getPluginManager().registerEvents(event, this);
        currentEvent = event;
        currentEvent.runTaskAsynchronously(this);
    }

    public void finishEvent(FreemodeEvent event) {
        if (currentEvent == event) {
            currentEvent.cancel();
            currentEvent = null;
            lastEventEnd = System.currentTimeMillis();
        }
        currentScoreboard = null;
        HandlerList.unregisterAll(event);

    }

    public boolean isRunningEvent(FreemodeEvent event) {
        return (currentEvent==event);
    }

    public boolean isRunningEvent() {
        return (currentEvent!=null);
    }

    public int getEventCooldown() {
        if (isRunningEvent()) return -1;
        return Math.toIntExact(eventCooldown - ((System.currentTimeMillis() - lastEventEnd) / 1000));
    }

    public FreemodeEvent getRunningEvent() { return currentEvent; }

    public void loadConfiguration() {
        eventCooldown = config.getConfig().getInt("event-cooldown");
    }

    public void setCurrentScoreboard(Scoreboard board) { this.currentScoreboard = board; }
    public Scoreboard getCurrentScoreboard() { return currentScoreboard; }
}
