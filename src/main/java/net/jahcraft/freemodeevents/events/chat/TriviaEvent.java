package net.jahcraft.freemodeevents.events.chat;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriviaEvent extends FreemodeEvent {

    private final TriviaQuestion question;
    private final int timeLimit;

    private Player winner = null;

    private List<Player> ignoring;

    public TriviaEvent() {
        this(getRandomQuestion(), Main.config.getConfig().getInt("trivia-timer"));
    }

    public TriviaEvent(TriviaQuestion question, int timeLimit) {
        this.question = question;
        this.timeLimit = timeLimit;
        this.ignoring = new ArrayList<>();
        Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (!event.getMessage().equalsIgnoreCase(question.correctAnswer)) {
            event.getPlayer().sendMessage(ChatColor.RED + "Incorrect answer!");
            ignoring.add(event.getPlayer());
            return;
        }

        event.setCancelled(true);
        winner = event.getPlayer();

        finish();

    }

    public void submitAnswer(Player p, String answer) {
        if (answer.equalsIgnoreCase(question.correctAnswer)) {
            winner = p;
            finish();
        } else {
            p.sendMessage(ChatColor.RED + "Incorrect answer!");
            ignoring.add(p);
        }
    }

    @Override
    public void run() {

        try {
            Bukkit.broadcastMessage("Trivia: " + question.question + " (Click to Answer)");
            for (String answer : question.answers()) {
                TextComponent msg = new TextComponent(TextComponent.fromLegacy("➠ §x§F§F§D§7§0§0" + answer));
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events trivia " + answer));
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§x§0§0§E§8§F§FClick to select this answer!")
                ));
                Bukkit.spigot().broadcast(msg);
            }

            Thread.sleep(1000L * timeLimit);
            if (Main.plugin.isRunningEvent(this)) {
                finish();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void finish() {

        Main.plugin.finishEvent(this);

        if (winner == null) {
            Bukkit.broadcastMessage("Nobody answered the question in time! (" + question.correctAnswer + ") Try again next time! ");
        } else {
            Bukkit.broadcastMessage(winner.getDisplayName() + " got the correct answer! (" + question.correctAnswer + ")");
        }
    }

    private static TriviaQuestion getRandomQuestion() {
        List<TriviaQuestion> questions = new ArrayList<>();
        List<Map<?,?>> configEntries = Main.config.getConfig().getMapList("trivia-questions");

        for (Map<?,?> entry : configEntries) {
            String question = (String) entry.get("question");
            List<String> answers = (List<String>) entry.get("answers");
            String correct = (String) entry.get("correct");

            if (question == null || answers == null || correct == null) {
                Bukkit.getLogger().warning("Invalid trivia question entry in config.yml!");
                continue;
            }

            questions.add(new TriviaQuestion(correct, answers, question));
        }
        return questions.get((int) (Math.random() * questions.size()));
    }

    public record TriviaQuestion(String correctAnswer, List<String> answers, String question) {}

}
