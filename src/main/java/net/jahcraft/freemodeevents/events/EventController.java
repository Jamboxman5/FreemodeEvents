package net.jahcraft.freemodeevents.events;

import net.jahcraft.freemodeevents.events.challenges.GravityStrikeEvent;
import net.jahcraft.freemodeevents.events.challenges.KillListEvent;
import net.jahcraft.freemodeevents.events.challenges.RampageEvent;
import net.jahcraft.freemodeevents.events.challenges.SniperChallengeEvent;
import net.jahcraft.freemodeevents.events.vip.ExecutiveSearchEvent;
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
            if (Main.plugin.canRunEvent(false)) {

                Main.plugin.runEvent(EventUtil.getRandomEvent());

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


}
