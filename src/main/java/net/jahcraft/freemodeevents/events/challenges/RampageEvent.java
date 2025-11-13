package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RampageEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetKills;
    private final List<EntityType> acceptableMobs;

    private final HashMap<Player, Integer> kills;

    private Scoreboard board;
    private Objective obj;

    public RampageEvent() {
        this(Main.config.getConfig().getInt("rampage-timer"), getMobsFromConfig(), Main.config.getConfig().getInt("rampage-target"));
    }

    public RampageEvent(int timeLimit, List<EntityType> acceptableMobs, int targetKills) {
        this.timeLimit = timeLimit;
        this.acceptableMobs = acceptableMobs;
        this.targetKills = targetKills;
        kills = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("rampagescores", Criteria.DUMMY, ChatColor.RED + "" + ChatColor.BOLD + "Rampage!");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        });


    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {


        if (!Main.plugin.isRunningEvent(this)) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.getHealth() - event.getDamage() > 0) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Mob)) return;
        if (!acceptableMobs.contains(event.getEntity().getType())) return;
        if (event.isCancelled()) return;

        if (!kills.containsKey(player)) kills.put(player, 1);
        else kills.put(player, kills.get(player) + 1);


        for (Player killer : kills.keySet()) {

            obj.getScore(killer.getDisplayName() + ChatColor.YELLOW + ": ").setScore(kills.get(killer));

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });


        if (targetKills <= 0) return;

        if (kills.get(player) < targetKills) return;

        finish();

    }

    @Override
    public void run() {

        if (targetKills > 0) {
            Bukkit.broadcastMessage("Rampage! The first player to reach " + targetKills + " kills within " + timeLimit + " seconds wins!");
        } else {
            Bukkit.broadcastMessage("Rampage! The player with the most mob kills after " + timeLimit + " seconds wins!");
        }

        try {
            Thread.sleep(1000L * timeLimit);
            if (!Main.plugin.isRunningEvent(this)) return;

            finish();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static List<EntityType> getMobsFromConfig() {
        List<String> configMobs = Main.config.getConfig().getStringList("rampage-mobs");
        List<EntityType> mobs = new ArrayList<>();
        for (String s : configMobs) {
            try {
                mobs.add(EntityType.valueOf(s));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid rampage mob! Fix configuration file!");
            }
        }
        return mobs;
    }

    @Override
    public void finish() {

        Main.plugin.finishEvent(this);

        Player highest = null;
        for (Player p : kills.keySet()) {
            if (highest == null) highest = p;
            if (kills.get(p) > kills.get(highest)) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players killed any mobs! Weak show!");
        } else if (targetKills <= 0) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " had the most mob kills at " + kills.get(highest) + "!");
        } else {
            Bukkit.broadcastMessage(highest.getDisplayName() + " was the first to reach " + targetKills + " kills!");
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
