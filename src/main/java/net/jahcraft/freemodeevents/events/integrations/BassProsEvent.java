package net.jahcraft.freemodeevents.events.integrations;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BassProsEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetFish;
    private final boolean require3star;

    private final HashMap<Player, Integer> fishes;

    private Scoreboard board;
    private Objective obj;

    public BassProsEvent() {
        this(Main.plugin.getConfig().getInt("bass-pros-timer"), Main.plugin.getConfig().getInt("bass-pros-target"), Main.plugin.getConfig().getBoolean("bass-pros-require-perfect"));
    }

    public BassProsEvent(int timeLimit, int targetFish, boolean require3star) {
        super("Bass Pros");
        this.timeLimit = timeLimit;
        this.targetFish = targetFish;
        this.require3star = require3star;
        fishes = new HashMap<>();

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            obj = board.registerNewObjective("bassproscores", Criteria.DUMMY, ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     " +
                    ChatColor.GRAY + "[ " +
                    ChatColor.of("#FFD700")+ "" + ChatColor.BOLD + "Bass Pros" +
                    ChatColor.GRAY + " ]" +
                    ChatColor.of("#007AD0") + "" +
                    ChatColor.STRIKETHROUGH + "     ");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        });

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishCaught(PlayerFishEvent e) {

        if (!Main.plugin.isRunningEvent(this)) return;
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Item caught = (Item) e.getCaught();

        if (!isFish(caught.getItemStack())) {
            return;
        }

        Player player = e.getPlayer();

        if (!fishes.containsKey(player)) fishes.put(player, 1);
        else fishes.put(player, fishes.get(player) + 1);

        List<Map.Entry<Player, Integer>> platform = fishes.entrySet()
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
            if (kills == 1) team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Fish");
            else team.setSuffix(ChatColor.of("#FFD700") + "" + kills + " Fish");

            obj.getScore(entry).setScore(i);

        }

        Bukkit.getScheduler().runTask(Main.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {

                p.setScoreboard(board);

            }
        });


        if (targetFish <= 0) return;

        if (fishes.get(player) < targetFish) return;

        finish();

    }

    private boolean isFish(ItemStack i) {
        if (i.getType() != Material.COD &&
                i.getType() != Material.PUFFERFISH &&
                i.getType() != Material.SALMON &&
                i.getType() != Material.TROPICAL_FISH) {
            return false;
        }

        List<String> lore = i.getItemMeta().getLore();
        if (require3star) {
            if (lore.get(0).contains("★★★")) return true;
        } else {
            if (lore.get(0).contains("☆") || lore.get(0).contains("★")) return true;
        }
        return true;
    }

    @Override
    public void run() {

        String stars = "";
        if (require3star) stars = " 3 star";

        if (targetFish > 0) {
            Bukkit.broadcastMessage("Bass Pros! The first player to catch " + targetFish + stars + " fish within " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
        } else {
            Bukkit.broadcastMessage("Bass Pros! The player to catch the most" + stars + " fish after " + EventUtil.secondsToMinutes(timeLimit) + " wins!");
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
        for (Player p : fishes.keySet()) {
            if (highest == null) highest = p;
            if (fishes.get(p) > fishes.get(highest)) highest = p;
        }

        if (highest == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players caught any fish! Weak show!");
        } else if (targetFish <= 0) {
            Bukkit.broadcastMessage(highest.getDisplayName() + " caught the most fish at " + fishes.get(highest) + "!");
        } else {
            Bukkit.broadcastMessage(highest.getDisplayName() + " was the first to reach " + targetFish + " fish!");
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
