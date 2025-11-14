package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.*;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GravityStrikeEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetDistance;
    private final boolean requireMace;

    private Player first;
    private Player second;
    private Player third;

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
            obj = board.registerNewObjective("gravitystrikescores", Criteria.DUMMY,
                    ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                            ChatColor.GRAY + "[ " +
                            ChatColor.of("#FFD700")+ "" + ChatColor.BOLD + "Gravity Strike" +
                            ChatColor.GRAY + " ]" +
                            ChatColor.of("#007AD0") + "" +
                            ChatColor.STRIKETHROUGH + "     ");
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

        if (distances.containsKey(player) && distances.get(player) > player.getFallDistance()) return;
        distances.put(player, player.getFallDistance());

        List<Map.Entry<Player, Float>> platform = distances.entrySet()
                .stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .limit(3)
                .toList();

        for (String entry : board.getEntries()) board.resetScores(entry);

        for (int i = platform.size(); i > 0; i--) {
//            Bukkit.broadcastMessage(i + " | " + platform.size());
            Map.Entry<Player, Float> position = platform.get(i-1);
            Player p = position.getKey();
            Float distance = position.getValue();

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

        Player highest = null;
        for (Player p : distances.keySet()) {
            if (highest == null) highest = p;
            if (distances.get(p) > distances.get(highest)) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players landed any hits! Weak show!");
        } else if (targetDistance <= 0) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " landed the highest hit from " + String.format("%.2f", distances.get(highest)) + "!");
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
