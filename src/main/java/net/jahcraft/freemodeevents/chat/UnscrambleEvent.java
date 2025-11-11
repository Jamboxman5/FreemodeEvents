package net.jahcraft.freemodeevents.chat;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

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

        ArrayList<Character> phraseList = new ArrayList<>();
        for (char c : phrase.toCharArray()) phraseList.add(c);

        Collections.shuffle(phraseList);

        StringBuilder scrambled = new StringBuilder();
        for (char c : phraseList) scrambled.append(c);

        Bukkit.broadcastMessage("Unscramble the following phrase to win a prize: " + scrambled);

    }
}
