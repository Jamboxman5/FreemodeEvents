package net.jahcraft.freemodeevents.events.vip;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
    private boolean fled = false;
    private Location lastLocation;
    private long lastMove;

    public ExecutiveSearchEvent(Player target, Location center) {
        this(target, center, Main.plugin.getConfig().getInt("executive-search-timer"), Main.plugin.getConfig().getInt("executive-search-beep-frequency"), Main.plugin.getConfig().getInt("executive-search-radius"));
    }

    public ExecutiveSearchEvent(Player target, Location center, int timeLimit, int beepFrequency, int radius) {
        super("Executive Search");
        this.timeLimit = timeLimit;
        this.beepFrequency = beepFrequency;
        this.radius = radius;

        executive = target;
        this.center = center;

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("executivesearchscores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
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
            team3.setPrefix(ChatColor.of("#007AD0") + "" +  ChatColor.STRIKETHROUGH + "                                           ");
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

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().getUniqueId() != executive.getUniqueId()) return;
        if (lastLocation == null) {
            lastLocation = event.getPlayer().getLocation();
            lastMove = System.currentTimeMillis();
            return;
        }
        Location newLoc = event.getPlayer().getLocation();
        if (newLoc.equals(lastLocation)) return;
        lastMove = System.currentTimeMillis();
        lastLocation = event.getPlayer().getLocation();
    }

    @Override
    public void run() {

        Bukkit.broadcastMessage(executive.getName() + " has started an Executive Search! Find and kill them within " + EventUtil.secondsToMinutes(timeLimit) + ". They are located within " + radius + " blocks of " + center.getBlockX() + ", " + center.getBlockZ() + ".");

        Main.plugin.setCurrentScoreboard(board);
        lastMove = System.currentTimeMillis();

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
                        double xComp = (int) (radius * Math.cos(Math.toRadians((i))));
                        double yComp = (int) (radius * Math.sin(Math.toRadians((i))));
                        double centerX = center.getBlockX();
                        double centerZ = center.getBlockZ();
                        double y = p.getLocation().getBlockY() + 1;
                        p.spawnParticle(Particle.RAID_OMEN, new Location(executive.getWorld(), centerX + xComp, y, centerZ + yComp), 8);
                        p.spawnParticle(Particle.RAID_OMEN, new Location(executive.getWorld(), centerX + xComp, y-1, centerZ + yComp), 8);
                    }
                }
            }

        });

        Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
            while (Main.plugin.isRunningEvent(this)) {

                try {
                    if (beepFrequency > 0) {
                        Thread.sleep(1000L * beepFrequency);
                    } else {
                        Thread.sleep(1000L);
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if ((System.currentTimeMillis() - lastMove) > (Main.plugin.getConfig().getInt("executive-search-afk-limit") * 1000L)) {
                    Bukkit.broadcastMessage(ChatColor.RED + "The executive was last spotted near " + lastLocation.getBlockX() + ", " + lastLocation.getBlockY() + ", " + lastLocation.getBlockZ() + "! ");
                    executive.sendMessage(ChatColor.RED + "You need to get moving! Don't stand still for too long or your location will be revealed! ");
                }

                if (!isWithinRadius(executive)) {
                    fled = true;
                    finish();
                } else {
                    Bukkit.getScheduler().runTask(Main.plugin, () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (!p.getUniqueId().equals(executive.getUniqueId()))
                                p.playSound(executive.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        }
                    });
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
        int x1 = center.getBlockX();
        int z1 = center.getBlockZ();
        int x2 = p.getLocation().getBlockX();
        int z2 = p.getLocation().getBlockZ();

        int distance = (int) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(z2 - z1, 2));
        return distance < radius;
    }

    @Override
    public void finish() {

        Main.lastExecutiveSearch = System.currentTimeMillis();
        Main.plugin.finishEvent(this);

        if (fled) {
            Bukkit.broadcastMessage(ChatColor.RED + executive.getName() + " has fled the search zone! Poor show!");
        }
        else if (winner != null) {
            Bukkit.broadcastMessage(winner.getName() + " has successfully hunted the executive!");
        }
        else {
            Bukkit.broadcastMessage(executive.getName() + " survived the Executive Search! Congratulations!");
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
