package net.jahcraft.freemodeevents.chat;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class UnscrambleEvent extends BukkitRunnable implements Listener {

    private String phrase;

    public UnscrambleEvent(String phrase) {
        this.phrase = phrase;
        Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (event.getMessage().equalsIgnoreCase(phrase)) event.setCancelled(true);
        Bukkit.broadcastMessage(event.getPlayer().getDisplayName() + " has unscrambled the phrase! (" + phrase + ")");
        Main.plugin.finishEvent(this);
        HandlerList.unregisterAll(this);
    }

    @Override
    public void run() {



        Bukkit.broadcastMessage("Unscramble the following phrase to win a prize: " + scramble("SPIGOTMC", true));

    }

    private String scramble(String word, boolean easyMode) {
        ArrayList<Character> phraseList = new ArrayList<>();
        for (char c : word.toCharArray()) phraseList.add(c);

        ArrayList<Character> scrambledList;
        if (!easyMode) {
            Collections.shuffle(phraseList);
            scrambledList = phraseList;
        } else {
            scrambledList = new ArrayList<>();
            scrambledList.add(phraseList.get(0));
            scrambledList.add(phraseList.get(phraseList.size() - 1));
            phraseList.remove(0);
            phraseList.remove(phraseList.size()-1);
            Collections.shuffle(phraseList);
            for (char c : phraseList) scrambledList.add(1, c);

        }

        StringBuilder scrambled = new StringBuilder();
        for (char c : scrambledList) scrambled.append(c);

        return scrambled.toString();
    }
}
