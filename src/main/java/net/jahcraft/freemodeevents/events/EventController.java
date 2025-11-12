package net.jahcraft.freemodeevents.events;

import net.jahcraft.freemodeevents.events.chat.UnscrambleEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class EventController extends BukkitRunnable {

    private boolean stopReceived = false;

    private int checkInterval;

    public EventController (int checkInterval) {
        //Interval in seconds to check for events
        this.checkInterval = checkInterval;
    }

    @Override
    public void run() {
        Bukkit.getLogger().info("Event Controller running!");

        while (!stopReceived) {
            if (Main.plugin.canRunEvent()) {

                Main.plugin.runEvent(new UnscrambleEvent("SpigotMC", 10));

            } else {
                try {
                    Thread.sleep(1000L * checkInterval);
                } catch (InterruptedException e) {
                    stopReceived = true;
                }
            }
        }

    }

    public void stop() { stopReceived = true; }

    private BukkitRunnable getRandomChatEvent() {
        return EventUtil.getGenericEvent();
    }

}
