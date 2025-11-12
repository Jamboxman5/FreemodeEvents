package net.jahcraft.freemodeevents.events;

import net.jahcraft.freemodeevents.events.challenges.RampageEvent;
import net.jahcraft.freemodeevents.events.chat.UnscrambleEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

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

                Main.plugin.runEvent(new RampageEvent());

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

    private BukkitRunnable getRandomEvent() {
        int i = (int) (Math.random() * 2);
        switch(i) {
            case 0:
                return new UnscrambleEvent();
            case 1:
                return new RampageEvent();
            default:
                return EventUtil.getGenericEvent();
        }
    }

}
