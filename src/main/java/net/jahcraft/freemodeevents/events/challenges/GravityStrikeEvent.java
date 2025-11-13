package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GravityStrikeEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetDistance;
    private final boolean requireMace;

    private final HashMap<Player, Float> distances;

    private Scoreboard board;
    private Objective obj;

    public GravityStrikeEvent() {
        this(Main.config.getConfig().getInt("gravity-strike-timer"),
                Main.config.getConfig().getBoolean("gravity-strike-require-mace"),
                Main.config.getConfig().getInt("gravity-strike-target"));
    }

    public GravityStrikeEvent(int timeLimit, boolean requireMace, int targetDistance) {
        this.timeLimit = timeLimit;
        this.requireMace = requireMace;
        this.targetDistance = targetDistance;
        distances = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("gravitystrikescores", Criteria.DUMMY, ChatColor.AQUA + "" + ChatColor.BOLD + "Gravity Strike");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        });


    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {


        if (!Main.plugin.isRunningEvent(this)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Mob)) return;
        if (player.getFallDistance() <= 1) return;
        if (event.isCancelled()) return;

        if (requireMace && player.getInventory().getItemInMainHand().getType() != Material.MACE) return;

        distances.put(player, player.getFallDistance());


        for (Player contestant : distances.keySet()) {

            obj.getScore(contestant.getDisplayName() + ChatColor.YELLOW + ": " + distances.get(contestant)).setScore(1);

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });


        if (targetDistance <= 0) return;

        if (distances.get(player) < targetDistance) return;

        finish();

    }

    @Override
    public void run() {

        if (targetDistance > 0) {
            Bukkit.broadcastMessage("Gravity Strike! The first player to land a hit from " + targetDistance + " blocks within " + timeLimit + " seconds wins!");
        } else {
            Bukkit.broadcastMessage("Gravity Strike! The player who lands the a hit from the highest distance within " + timeLimit + " seconds wins!");
        }

        try {
            Thread.sleep(1000L * timeLimit);
            if (!Main.plugin.isRunningEvent(this)) return;

            finish();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void finish() {

        Main.plugin.finishEvent(this);

        Player highest = null;
        for (Player p : distances.keySet()) {
            if (highest == null) highest = p;
            if (distances.get(p) > distances.get(highest)) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players landed any hits! Weak show!");
        } else if (targetDistance <= 0) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " landed the highest hit from " + distances.get(highest) + "!");
        } else {
            Bukkit.broadcastMessage(highest.getDisplayName() + " was the first to land a hit from " + targetDistance + " blocks!");
        }


        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {

            try {
                Thread.sleep(1000 * 5);

                Bukkit.getScheduler().runTask(Main.plugin, () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {

                        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }
                });

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });






    }
}
