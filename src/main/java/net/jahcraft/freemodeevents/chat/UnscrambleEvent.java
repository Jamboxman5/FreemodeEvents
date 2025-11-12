package net.jahcraft.freemodeevents.chat;

import net.jahcraft.freemodeevents.main.FreemodeEvent;
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

public class UnscrambleEvent extends FreemodeEvent {

    private String phrase;
    private int timeLimit;

    public UnscrambleEvent(String phrase, int timeLimit) {
        this.phrase = phrase;
        this.timeLimit = timeLimit;

        Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (event.getMessage().equalsIgnoreCase(phrase)) event.setCancelled(true);
        Bukkit.broadcastMessage(event.getPlayer().getDisplayName() + " has unscrambled the phrase! (" + phrase + ")");
        Main.plugin.finishEvent(this);
    }

    @Override
    public void run() {


        try {
            Bukkit.broadcastMessage("Unscramble the following phrase to win a prize: " + scramble(phrase, true));
            Thread.sleep(1000 * timeLimit);
            if (Main.plugin.isRunningEvent(this)) {
                Bukkit.broadcastMessage("Nobody unscrambled the phrase in time! (" + phrase + ") Try again next time! ");
                Main.plugin.finishEvent(this);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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

        return scrambled.toString().toUpperCase();
    }
}
