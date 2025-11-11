package net.jahcraft.freemodeevents.main;

import net.jahcraft.freemodeevents.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class EventController extends BukkitRunnable {

    private boolean stopReceived = false;

    private int interval;

    public EventController (int interval) {
        //Interval in seconds
        this.interval = interval;
    }

    @Override
    public void run() {
        Bukkit.getLogger().info("Event Controller running!");

        while (!stopReceived) {
            if (Main.plugin.canRunEvent()) {

                Main.plugin.runEvent(getRandomChatEvent());

            } else {
                try {
                    Thread.sleep(1000L * interval);
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
