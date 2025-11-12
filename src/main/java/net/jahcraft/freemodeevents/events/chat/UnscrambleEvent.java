package net.jahcraft.freemodeevents.events.chat;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnscrambleEvent extends FreemodeEvent {

    private final String phrase;
    private final int timeLimit;
    private final boolean easyMode;

    public UnscrambleEvent() {
        List<String> phrases = Main.config.getConfig().getStringList("unscramble-phrases");
        this.phrase = phrases.get((int) (Math.random() * phrases.size()));
        this.timeLimit = Main.config.getConfig().getInt("unscramble-timer");

        int selectedDifficulty = Main.config.getConfig().getInt("unscramble-difficulty");
        if (selectedDifficulty == 0) easyMode = true;
        else if (selectedDifficulty == 1) easyMode = false;
        else {
            double hardChance = Main.config.getConfig().getDouble("unscramble-difficulty-chance");
            easyMode = (Math.random() > hardChance);
        }
    }

    public UnscrambleEvent(String phrase, int timeLimit, boolean easyMode) {
        this.phrase = phrase;
        this.timeLimit = timeLimit;
        this.easyMode = easyMode;

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
            Bukkit.broadcastMessage("Unscramble the following phrase to win a prize: " + scramble(phrase));
            Thread.sleep(1000L * timeLimit);
            if (Main.plugin.isRunningEvent(this)) {
                Bukkit.broadcastMessage("Nobody unscrambled the phrase in time! (" + phrase + ") Try again next time! ");
                Main.plugin.finishEvent(this);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private String scramble(String word) {
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
