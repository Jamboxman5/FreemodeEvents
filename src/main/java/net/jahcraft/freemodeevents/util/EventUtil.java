package net.jahcraft.freemodeevents.util;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;

public class EventUtil {

    public static FreemodeEvent getGenericEvent() {
        return new FreemodeEvent() {
            @Override
            public void finish() {
                Main.plugin.finishEvent(this);
                Bukkit.getLogger().info("Event finished.");
            }

            @Override
            public void run() {
                Bukkit.broadcastMessage("This is a generic event! Hello everyone!");
                finish();
            }
        };
    }

    public static String secondsToMinutes(int seconds) {
        int minutes = seconds / 60;
        int leftover = seconds % 60;
        if (seconds < 60) return seconds + " seconds";
        if (minutes == 1) {
            if (leftover == 0) return minutes + " minute";
            return minutes + " minute & " + leftover + " seconds";
        } else {
            if (leftover == 0) return minutes + " minutes";
            return minutes + " minutes & " + leftover + " seconds";
        }

    }
}
