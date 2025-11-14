package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.*;

import java.util.*;

public class KillListEvent extends FreemodeEvent {

    private final int timeLimit;
    private final HashSet<EntityType> killList;

    private final HashMap<Player, HashSet<EntityType>> kills;

    private Scoreboard board;
    private Objective obj;

    public KillListEvent() {
        this(Main.config.getConfig().getInt("kill-list-timer"),
                generateKillList(getMobsFromConfig(), Main.config.getConfig().getInt("kill-list-size")));
    }

    public KillListEvent(int timeLimit,
                         HashSet<EntityType> killList) {
        this.timeLimit = timeLimit;
        this.killList = killList;
        kills = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("killlistscores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                    ChatColor.GRAY + "[ " +
                    ChatColor.of("#FFD700")+ "" + ChatColor.BOLD + "Kill List" +
                    ChatColor.GRAY + " ]" +
                    ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     ");
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
        if (!killList.contains(event.getEntity().getType())) return;
        if (event.isCancelled()) return;

        if (!kills.containsKey(player)) kills.put(player, new HashSet<>());

        kills.get(player).add(event.getEntity().getType());

        List<Map.Entry<Player, HashSet<EntityType>>> platform = kills.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(3)
                .toList();

        for (String entry : board.getEntries()) board.resetScores(entry);


        for (int i = platform.size(); i > 0; i--) {
//            Bukkit.broadcastMessage(i + " | " + platform.size());
            Map.Entry<Player, HashSet<EntityType>> position = platform.get(i-1);
            Player p = position.getKey();
            Integer kills = position.getValue().size();

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
            if (kills == 1) team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Kill");
            else team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Kills");

            obj.getScore(entry).setScore(i);

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });

        if (kills.get(player).size() < killList.size()) return;

        finish();

    }

    @Override
    public void run() {

        Bukkit.broadcastMessage("Kill List! The first player to kill one of each mob within " + timeLimit + " seconds wins!");
        StringBuilder mobs = new StringBuilder();
        for (EntityType type : killList) mobs.append(getFormattedName(type)).append(", ");
        Bukkit.broadcastMessage("The mobs to kill are: " + mobs.substring(0, mobs.toString().length()-2));

        Main.plugin.setCurrentScoreboard(board);


        try {
            Thread.sleep(1000L * timeLimit);
            if (!Main.plugin.isRunningEvent(this)) return;

            finish();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private String getFormattedName(EntityType type) {
        String s = type.toString().toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (String split : s.split("_")) {
            String formatted = split.substring(0, 1).toUpperCase() + split.substring(1);
            builder.append(formatted).append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    private static List<EntityType> getMobsFromConfig() {
        List<String> configMobs = Main.config.getConfig().getStringList("kill-list-mobs");
        List<EntityType> mobs = new ArrayList<>();
        for (String s : configMobs) {
            try {
                mobs.add(EntityType.valueOf(s));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid kill list mob! Fix configuration file!");
            }
        }
        return mobs;
    }

    private static HashSet<EntityType> generateKillList(List<EntityType> selectionList, int listSize) {
        HashSet<EntityType> mobs = new HashSet<>();
        Collections.shuffle(selectionList);
        if (listSize > selectionList.size()) listSize = selectionList.size();
        for (int i = 0; i < listSize; i++) {
            mobs.add(selectionList.get(i));
        }
        return mobs;
    }

    @Override
    public void finish() {

        Main.plugin.finishEvent(this);

        Player highest = null;
        for (Player p : kills.keySet()) {
            if (highest == null) highest = p;
            if (kills.get(p).size() > kills.get(highest).size()) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players killed any mobs! Weak show!");
        } else if (kills.get(highest).size() != killList.size()) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " had the most mob kills at " + kills.get(highest).size() + "!");
        } else {
            Bukkit.broadcastMessage(highest.getDisplayName() + " was the first to complete the kill list!");
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
