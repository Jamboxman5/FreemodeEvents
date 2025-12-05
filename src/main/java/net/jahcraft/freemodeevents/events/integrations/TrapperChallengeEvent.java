package net.jahcraft.freemodeevents.events.integrations;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrapperChallengeEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetPelts;
    private final boolean require3star;

    private final HashMap<Player, Integer> pelts;
    private final HashMap<LivingEntity, Player> lastAttacks;

    private Scoreboard board;
    private Objective obj;

    public TrapperChallengeEvent() {
        this(Main.config.getConfig().getInt("trapper-challenge-timer"), Main.config.getConfig().getInt("trapper-challenge-target"), Main.config.getConfig().getBoolean("trapper-challenge-require-perfect"));
    }

    public TrapperChallengeEvent(int timeLimit, int targetPelts, boolean require3star) {
        super("Trapper Challenge");
        this.timeLimit = timeLimit;
        this.targetPelts = targetPelts;
        this.require3star = require3star;
        pelts = new HashMap<>();
        lastAttacks = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("trapperscores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                    ChatColor.GRAY + "[ " +
                    ChatColor.of("#FFD700")+ "" + ChatColor.BOLD + "Trapper Challenge" +
                    ChatColor.GRAY + " ]" +
                    ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     ");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        });

    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (!lastAttacks.containsKey(e.getEntity())) return;
        if (!hasPelt(e.getDrops(), require3star)) return;

        Player player = lastAttacks.get(e.getEntity());

        if (!pelts.containsKey(player)) pelts.put(player, 1);
        else pelts.put(player, pelts.get(player) + 1);

        List<Map.Entry<Player, Integer>> platform = pelts.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .toList();

        for (String entry : board.getEntries()) board.resetScores(entry);


        for (int i = platform.size(); i > 0; i--) {
//            Bukkit.broadcastMessage(i + " | " + platform.size());
            Map.Entry<Player, Integer> position = platform.get(i-1);
            Player p = position.getKey();
            Integer kills = position.getValue();

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
            if (kills == 1) team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Pelt");
            else team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Pelts");

            obj.getScore(entry).setScore(i);

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });


        if (targetPelts <= 0) return;

        if (pelts.get(player) < targetPelts) return;

        finish();

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {


        if (!Main.plugin.isRunningEvent(this)) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (livingEntity.getHealth() - event.getDamage() > 0) return;
        Player player;
        if (event.getDamager() instanceof Projectile proj) {
            if (!(proj.getShooter() instanceof Player )) return;
            player = (Player) proj.getShooter();
        } else {
            if (!(event.getDamager() instanceof Player)) return;
            player = (Player) event.getDamager();
        }
        if (event.isCancelled()) return;

        lastAttacks.put(livingEntity, player);

    }

    private boolean hasPelt(List<ItemStack> drops, boolean requireThreeStar) {
        for (ItemStack drop : drops) {
            if (drop.getType() == Material.LEATHER && drop.hasItemMeta() && drop.getItemMeta().hasLore()) {
                List<String> lore = drop.getItemMeta().getLore();
                if (requireThreeStar) {
                    if (lore.get(0).contains("★★★")) return true;
                } else {
                    if (lore.get(0).contains("☆") || lore.get(0).contains("★")) return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {

        String stars = "";
        if (require3star) stars = " 3 star";

        if (targetPelts > 0) {
            Bukkit.broadcastMessage("Trapper Challenge! The first player to harvest " + targetPelts + stars + " pelts within " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
        } else {
            Bukkit.broadcastMessage("Trapper Challenge! The player to harvest the most" + stars + " pelts after " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
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
        for (Player p : pelts.keySet()) {
            if (highest == null) highest = p;
            if (pelts.get(p) > pelts.get(highest)) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players harvested any pelts! Weak show!");
        } else if (targetPelts <= 0) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " harvested the most pelts at " + pelts.get(highest) + "!");
        } else {
            Bukkit.broadcastMessage(highest.getDisplayName() + " was the first to reach " + targetPelts + " pelts!");
        }
        Main.plugin.addWin(highest, this);


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
