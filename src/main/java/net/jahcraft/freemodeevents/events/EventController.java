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

                Main.plugin.runEvent(getRandomEvent());

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

    private FreemodeEvent getRandomEvent() {
        int unscrambleWeight = Main.config.getConfig().getInt("unscramble-weight");
        int rampageWeight = Main.config.getConfig().getInt("rampage-weight");
        int gravityStrikeWeight = Main.config.getConfig().getInt("gravity-strike-weight");
        int killListWeight = Main.config.getConfig().getInt("kill-list-weight");
        int sniperChallengeWeight = Main.config.getConfig().getInt("sniper-challenge-weight");

        int total = unscrambleWeight + rampageWeight + gravityStrikeWeight + killListWeight + sniperChallengeWeight;
        int roll = (int) (Math.random() * total);
        int counter = 0;

        counter += unscrambleWeight;
        if (roll < counter) return new UnscrambleEvent();

        counter += rampageWeight;
        if (roll < counter) return new RampageEvent();

        counter += gravityStrikeWeight;
        if (roll < counter) return new GravityStrikeEvent();

        counter += killListWeight;
        if (roll < counter) return new KillListEvent();

        counter += sniperChallengeWeight;
        if (roll < counter) return new SniperChallengeEvent();

        return EventUtil.getGenericEvent();

    }

}
