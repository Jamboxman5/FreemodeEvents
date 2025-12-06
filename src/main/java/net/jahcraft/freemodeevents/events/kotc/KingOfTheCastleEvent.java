package net.jahcraft.freemodeevents.events.kotc;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

public class KingOfTheCastleEvent extends FreemodeEvent {

    private final int timeLimit;
    private final KOTHLocation location;

    private Player holding;

    private Scoreboard board;
    private Objective obj;
    private Player winner = null;

    private final HashMap<Player, Integer> scores;
    private final HashMap<Player, Long> enteredTimes;

    public KingOfTheCastleEvent(KOTHLocation location, int timeLimit) {
        super("Executive Search");
        this.timeLimit = timeLimit;
        this.location = location;

        scores = new HashMap<>();
        enteredTimes = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("kothscores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                    ChatColor.GRAY + "[ " +
                    ChatColor.of("#FFD700") + ChatColor.BOLD + "King of the Castle" +
                    ChatColor.GRAY + " ]" +
                    ChatColor.of("#007AD0") +
                    ChatColor.STRIKETHROUGH + "     ");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team team1 = board.registerNewTeam("line1");
            Team team2 = board.registerNewTeam("line2");
            Team team3 = board.registerNewTeam("line3");

            String team1Entry = ChatColor.AQUA + "";
            String team2Entry = ChatColor.DARK_AQUA + "";
            String team3Entry = ChatColor.BLUE + "";

            team1.addEntry(team1Entry);
            team2.addEntry(team2Entry);
            team3.addEntry(team3Entry);


            team1.setPrefix(ChatColor.of("#49B3FF") + "Hold and defend the area at");
            team3.setPrefix(ChatColor.of("#007AD0") + "" +  ChatColor.STRIKETHROUGH + "                                           ");
            team2.setPrefix(ChatColor.of("#FFD700") + "" + location.center.getBlockX() + ChatColor.of("#49B3FF") + ", " + ChatColor.of("#FFD700") + location.center.getBlockY() + ChatColor.of("#49B3FF") + ", " + ChatColor.of("#FFD700") + location.center.getBlockZ());

            obj.getScore(team1Entry).setScore(3);
            obj.getScore(team2Entry).setScore(2);
            obj.getScore(team3Entry).setScore(0);

            obj.getScore(ChatColor.YELLOW + "").setScore(4);
            obj.getScore(ChatColor.RED + "").setScore(1);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(board);
            }

        });


    }

    @Override
    public void run() {

        Bukkit.broadcastMessage("King of the Hill! The player to hold the zone at " +
                ChatColor.of("#FFD700") + location.center.getBlockX() + ChatColor.of("#49B3FF") + ", " + ChatColor.of("#FFD700") + location.center.getBlockY() + ChatColor.of("#49B3FF") + ", " + ChatColor.of("#FFD700") + location.center.getBlockZ() +
                ChatColor.WHITE + " for the longest time wins!");

        Main.plugin.setCurrentScoreboard(board);

        Bukkit.getScheduler().runTask(Main.plugin, () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(board);
            }

        });

        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
            while (Main.plugin.isRunningEvent(this)) {

                try {
                    Thread.sleep(1000L);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isWithinRadius(p)) continue;
                    for (int i = 0; i < 360; i++) {
                        double xComp = (int) (location.radius * Math.cos(Math.toRadians((i))));
                        double yComp = (int) (location.radius * Math.sin(Math.toRadians((i))));
                        double centerX = location.center.getBlockX();
                        double centerZ = location.center.getBlockZ();
                        double y = p.getLocation().getBlockY() + 1;
                        p.spawnParticle(Particle.RAID_OMEN, new Location(location.center.getWorld(), centerX + xComp, y, centerZ + yComp), 8);
                        p.spawnParticle(Particle.RAID_OMEN, new Location(location.center.getWorld(), centerX + xComp, y-1, centerZ + yComp), 8);
                    }
                }
            }

        });

        try {

            Thread.sleep(1000L * timeLimit);
            if (!Main.plugin.isRunningEvent(this)) return;

            finish();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean isWithinRadius(Player p) {
        int x1 = location.center.getBlockX();
        int z1 = location.center.getBlockZ();
        int x2 = p.getLocation().getBlockX();
        int z2 = p.getLocation().getBlockZ();

        int distance = (int) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(z2 - z1, 2));
        return distance < location.radius;
    }

    @Override
    public void finish() {

        Main.lastExecutiveSearch = System.currentTimeMillis();
        Main.plugin.finishEvent(this);

        if (winner != null) {
            Bukkit.broadcastMessage(winner.getName() + " has successfully hunted the executive!");
        }
        else {
            Bukkit.broadcastMessage("Nobody held the zone! Poor show!");
        }

        Main.plugin.addWin(winner, this);



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
