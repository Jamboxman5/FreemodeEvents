package net.jahcraft.freemodeevents.util;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;

public class EventUtil {

    public static FreemodeEvent getGenericEvent() {
        return new FreemodeEvent() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("This is a generic event! Hello everyone!");

                Bukkit.getLogger().info("Event finished.");
                Main.plugin.finishEvent(this);
            }
        };
    }
}
