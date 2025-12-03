package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeToMineEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetCount;
    private final Material target;

    private final HashMap<Player, Integer> blocks;

    private Scoreboard board;
    private Objective obj;

    private List<Location> placedLocs;

    public TimeToMineEvent() {
        this(Main.config.getConfig().getInt("time-to-mine-timer"), getTargetBlock(), Main.config.getConfig().getInt("time-to-mine-target"));
    }

    public TimeToMineEvent(int timeLimit, Material target, int targetCount) {
        super("Time to Mine");
        this.timeLimit = timeLimit;
        this.target = target;
        this.targetCount = targetCount;
        blocks = new HashMap<>();
        placedLocs = new ArrayList<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("timetominescores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                    ChatColor.GRAY + "[ " +
                    ChatColor.of("#FFD700")+ "" + ChatColor.BOLD + "Time to Mine!" +
                    ChatColor.GRAY + " ]" +
                    ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     ");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        });


    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (!target.equals(event.getBlock().getType())) return;
        if (event.isCancelled()) return;

        placedLocs.add(event.getBlock().getLocation());
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {


        if (!Main.plugin.isRunningEvent(this)) return;
        if (!target.equals(event.getBlock().getType())) return;
        if (placedLocs.contains(event.getBlock().getLocation())) return;
        if (event.isCancelled()) return;

        Player player = event.getPlayer();

        if (!blocks.containsKey(player)) blocks.put(player, 1);
        else blocks.put(player, blocks.get(player) + 1);

        List<Map.Entry<Player, Integer>> platform = blocks.entrySet()
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
            if (kills == 1) team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Block");
            else team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Blocks");

            obj.getScore(entry).setScore(i);

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });


        if (targetCount <= 0) return;

        if (blocks.get(player) < targetCount) return;

        finish();

    }

    @Override
    public void run() {

        if (targetCount > 0) {
            Bukkit.broadcastMessage("Time to Mine! The first player to mine " + targetCount + " " + EventUtil.getFormattedName(target) + " within " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
        } else {
            Bukkit.broadcastMessage("Time to Mine! The player to mine the most " + EventUtil.getFormattedName(target) + " within " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
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

    private static Material getTargetBlock() {
        List<String> configBlocks = Main.config.getConfig().getStringList("time-to-mine-blocks");
        List<Material> blocks = new ArrayList<>();
        for (String s : configBlocks) {
            try {
                blocks.add(Material.valueOf(s));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid time to mine block! Fix configuration file!");
            }
        }
        return blocks.get((int) (Math.random() * blocks.size()));
    }

    @Override
    public void finish() {

        Main.plugin.finishEvent(this);

        Player highest = null;
        for (Player p : blocks.keySet()) {
            if (highest == null) highest = p;
            if (blocks.get(p) > blocks.get(highest)) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players mined any " + EventUtil.getFormattedName(target) + "! Weak show!");
        } else if (targetCount <= 0) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " mined the most " + EventUtil.getFormattedName(target) + " at " + blocks.get(highest) + "!");
        } else {
            Bukkit.broadcastMessage(highest.getDisplayName() + " was the first to mine " + targetCount + " " + EventUtil.getFormattedName(target) + "!");
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
