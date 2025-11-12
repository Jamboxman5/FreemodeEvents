package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RampageEvent extends FreemodeEvent {

    private final int timeLimit;
    private final int targetKills;
    private final List<EntityType> acceptableMobs;

    private final HashMap<Player, Integer> kills;

    public RampageEvent() {
        this(Main.config.getConfig().getInt("rampage-timer"), getMobsFromConfig(), Main.config.getConfig().getInt("rampage-target"));
    }

    public RampageEvent(int timeLimit, List<EntityType> acceptableMobs, int targetKills) {
        this.timeLimit = timeLimit;
        this.acceptableMobs = acceptableMobs;
        this.targetKills = targetKills;
        kills = new HashMap<>();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (!event.getEntity().isDead()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getEntity() instanceof Mob)) return;
        if (!acceptableMobs.contains(event.getEntity().getType())) return;
        if (event.isCancelled()) return;

        Player player = (Player) event.getDamager();

        if (!kills.containsKey(player)) kills.put(player, 1);
        else kills.put(player, kills.get(player) + 1);

        if (targetKills <= 0) return;

        if (kills.get(player) < targetKills) return;

        Bukkit.broadcastMessage(player.getDisplayName() + " was the first to reach " + targetKills + " kills!");
        Main.plugin.finishEvent(this);

    }

    @Override
    public void run() {

        Bukkit.broadcastMessage("Rampage! The player with the most mob kills after " + timeLimit + " seconds wins!");

        try {
            Thread.sleep(1000L * timeLimit);
            if (!Main.plugin.isRunningEvent(this)) return;

            Player highest = null;
            for (Player p : kills.keySet()) {
                if (highest == null) highest = p;
                if (kills.get(p) > kills.get(highest)) highest = p;
            }

            if (highest == null) {
                Bukkit.broadcastMessage(ChatColor.RED + "No players killed any mobs! Weak show!");
            } else {
                Bukkit.broadcastMessage(highest.getDisplayName() + " had the most mob kills at " + kills.get(highest) + "!");
            }

            Main.plugin.finishEvent(this);

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
}
