package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SniperChallengeEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetDistance;

    private Player first;
    private Player second;
    private Player third;

    private final HashMap<Player, Double> distances;

    private Scoreboard board;
    private Objective obj;

    public SniperChallengeEvent() {
        this(Main.config.getConfig().getInt("sniper-challenge-timer"),
                Main.config.getConfig().getInt("gravity-strike-target"));
    }

    public SniperChallengeEvent(int timeLimit, int targetDistance) {
        this.timeLimit = timeLimit;
        this.targetDistance = targetDistance;
        distances = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("sniperchallengescores", Criteria.DUMMY,
                    ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                            ChatColor.GRAY + "[ " +
                            ChatColor.of("#FFD700")+ ChatColor.BOLD + "Sniper Challenge" +
                            ChatColor.GRAY + " ]" +
                            ChatColor.of("#007AD0") +
                            ChatColor.STRIKETHROUGH + "     ");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        });


    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {

        if (!Main.plugin.isRunningEvent(this)) return;
        if (event.getHitEntity() == null) return;
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof LivingEntity target)) return;

        Location shooterLoc = shooter.getLocation();
        Location targetLoc = target.getLocation();
        double shootDistance = shooterLoc.distance(targetLoc);

        if (event.isCancelled()) return;

        shooter.sendMessage("Hit! Distance: " + String.format("%.2f", shootDistance) + "m");

        if (distances.containsKey(shooter) && distances.get(shooter) > shootDistance) return;
        distances.put(shooter, shootDistance);

        List<Map.Entry<Player, Double>> platform = distances.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(3)
                .toList();

        for (String entry : board.getEntries()) board.resetScores(entry);

        for (int i = platform.size(); i > 0; i--) {
//            Bukkit.broadcastMessage(i + " | " + platform.size());
            Map.Entry<Player, Double> position = platform.get(i-1);
            Player p = position.getKey();
            Double distance = position.getValue();

            String entry = "";
            switch(i) {
                case 3:
                    entry += ChatColor.RED;
                    break;
                case 2:
                    entry += ChatColor.GREEN;
                    break;
                case 1:
                    entry += ChatColor.BLUE;
                    break;
                default:
                    entry += ChatColor.BLACK;
                    break;
            }

            Team team = board.getTeam("rank" + i);
            if (team == null) team = board.registerNewTeam("rank" + i);

            team.addEntry(entry);
            team.setPrefix(ChatColor.of("#49B3FF") + p.getName() + ": ");
            team.setSuffix(ChatColor.of("#FFD700") + String.format("%.2f", distance) + "m");

            obj.getScore(entry).setScore(i);

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });


        if (targetDistance <= 0) return;

        if (distances.get(shooter) < targetDistance) return;

        finish();

    }

    @Override
    public void run() {

        if (targetDistance > 0) {
            Bukkit.broadcastMessage("Sniper Challenge! The first player to land a projectile hit from " + targetDistance + " blocks away within " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
        } else {
            Bukkit.broadcastMessage("Sniper Challenge! The player who lands a projectile hit from the farthest distance within " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
        }

        Main.plugin.setCurrentScoreboard(board);

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

        Player farthest = null;
        for (Player p : distances.keySet()) {
            if (farthest == null) farthest = p;
            if (distances.get(p) > distances.get(farthest)) farthest = p;
        }

        if (farthest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players landed any snipes! Weak show!");
        } else if (targetDistance <= 0) {
            Bukkit.broadcastMessage(farthest.getDisplayName() + " landed the longest snipe from " + String.format("%.2f", distances.get(farthest)) + "!");
        } else {
            Bukkit.broadcastMessage(farthest.getDisplayName() + " was the first to land a snipe from " + targetDistance + " blocks!");
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
