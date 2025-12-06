package net.jahcraft.freemodeevents.util;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.events.challenges.*;
import net.jahcraft.freemodeevents.events.chat.TriviaEvent;
import net.jahcraft.freemodeevents.events.chat.UnscrambleEvent;
import net.jahcraft.freemodeevents.events.integrations.BassProsEvent;
import net.jahcraft.freemodeevents.events.integrations.TrapperChallengeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public class EventUtil {

    public static FreemodeEvent getGenericEvent() {
        return new FreemodeEvent("Generic") {
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

    public static FreemodeEvent getRandomEvent() {
        int unscrambleWeight = Main.plugin.getConfig().getInt("unscramble-weight");
        int rampageWeight = Main.plugin.getConfig().getInt("rampage-weight");
        int gravityStrikeWeight = Main.plugin.getConfig().getInt("gravity-strike-weight");
        int killListWeight = Main.plugin.getConfig().getInt("kill-list-weight");
        int sniperChallengeWeight = Main.plugin.getConfig().getInt("sniper-challenge-weight");
        int timeToMineWeight = Main.plugin.getConfig().getInt("time-to-mine-weight");
        int triviaWeight = Main.plugin.getConfig().getInt("trivia-weight");
        int trapperChallengeWeight = Main.plugin.getConfig().getInt("trapper-challenge-weight");
        int bassProsWeight = Main.plugin.getConfig().getInt("bass-pros-weight");

        int total = unscrambleWeight + rampageWeight + gravityStrikeWeight + killListWeight + sniperChallengeWeight + timeToMineWeight + triviaWeight + trapperChallengeWeight + bassProsWeight;


        if (!hasWesternHunting()) {
            if (trapperChallengeWeight > 0) {
                Bukkit.getLogger().warning("WesternHunting not detected! You can't run integration events without the required plugin! ");
                Bukkit.getLogger().warning("Download WesternHunting here: https://www.spigotmc.org/resources/westernhunting-wip.130522/");
                Bukkit.getLogger().warning("Disabling integration events...");
                total -= trapperChallengeWeight;
                total -= bassProsWeight;
            }
        }

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

        counter += timeToMineWeight;
        if (roll < counter) return new TimeToMineEvent();

        counter += triviaWeight;
        if (roll < counter) return new TriviaEvent();

        if (hasWesternHunting()) {
            counter += trapperChallengeWeight;
            if (roll < counter) return new TrapperChallengeEvent();
        }

        if (hasWesternHunting()) {
            counter += bassProsWeight;
            if (roll < counter) return new BassProsEvent();
        }

        return EventUtil.getGenericEvent();

    }

    public static boolean hasWesternHunting() {
        Plugin westernHunting = Main.plugin.getServer().getPluginManager().getPlugin("WesternHunting");
        return (westernHunting != null && westernHunting.isEnabled());
    }
}
