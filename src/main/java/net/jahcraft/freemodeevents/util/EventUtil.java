package net.jahcraft.freemodeevents.util;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

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

    public static String getFormattedName(EntityType type) { return getFormattedName(type.toString()); }
    public static String getFormattedName(Material type) { return getFormattedName(type.toString()); }

    private static String getFormattedName(String enumString) {
        String s = enumString.toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (String split : s.split("_")) {
            String formatted = split.substring(0, 1).toUpperCase() + split.substring(1);
            builder.append(formatted).append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
