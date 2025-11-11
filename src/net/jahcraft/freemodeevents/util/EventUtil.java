package net.jahcraft.freemodeevents.util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class EventUtil {

    public static BukkitRunnable getGenericEvent() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("This is a generic event! Hello everyone!");
            }
        };
    }
}
