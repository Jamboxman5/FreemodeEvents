package net.jahcraft.freemodeevents.events.vip;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.*;

public class ExecutiveSearchEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int beepFrequency;
    private final Location center;
    private final int radius;

    private final Player executive;

    private Scoreboard board;
    private Objective obj;
    private Player winner = null;

    public ExecutiveSearchEvent(Player target, Location center) {
        this(target, center, Main.config.getConfig().getInt("executive-search-timer"), Main.config.getConfig().getInt("executive-search-beep-frequency"), Main.config.getConfig().getInt("executive-search-radius"));
    }

    public ExecutiveSearchEvent(Player target, Location center, int timeLimit, int beepFrequency, int radius) {
        this.timeLimit = timeLimit;
        this.beepFrequency = beepFrequency;
        this.radius = radius;

        executive = target;
        this.center = center;

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("rampagescores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                    ChatColor.GRAY + "[ " +
                    ChatColor.of("#FFD700") + ChatColor.BOLD + "Executive Search" +
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


            team1.setPrefix(ChatColor.of("#49B3FF") + "Hunt down the executive located");
            team3.setPrefix(ChatColor.of("#007AD0") + "" +  ChatColor.STRIKETHROUGH + "             ");
            team2.setPrefix(ChatColor.of("#49B3FF") + "within " + ChatColor.of("#FFD700") + radius + ChatColor.of("#49B3FF") + " blocks of " +
                    ChatColor.of("#FFD700") + center.getBlockX() + ChatColor.of("#49B3FF") + ", " + ChatColor.of("#FFD700") + center.getBlockZ());

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

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {


        if (!Main.plugin.isRunningEvent(this)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (target.getHealth() - event.getDamage() > 0) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!target.getUniqueId().equals(executive.getUniqueId())) return;
        if (event.isCancelled()) return;

        winner = player;

        finish();

    }

    @Override
    public void run() {

        Bukkit.broadcastMessage(executive.getName() + " has started an Executive Search! Find and kill them. They are located within " + radius + " blocks of " + center.getBlockX() + ", " + center.getBlockZ() + ".");

        Main.plugin.setCurrentScoreboard(board);

        Bukkit.getScheduler().runTask(Main.plugin, () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(board);
            }

        });

        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
            while (Main.plugin.isRunningEvent(this)) {

                try {
                    Thread.sleep(1000L * beepFrequency);
                    Bukkit.getScheduler().runTask(Main.plugin, () -> {
                        executive.getLocation().getWorld().playSound(executive.getLocation(), Sound.UI_TOAST_IN, 1f, 1f);
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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

    @Override
    public void finish() {

        Main.plugin.finishEvent(this);

        if (winner != null) {
            Bukkit.broadcastMessage(winner.getName() + " has successfully hunted the executive!");
        } else {
            Bukkit.broadcastMessage(executive.getName() + " survived the Executive Search! Congratulations!");
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
