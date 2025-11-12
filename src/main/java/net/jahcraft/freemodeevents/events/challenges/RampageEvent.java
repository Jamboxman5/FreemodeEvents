package net.jahcraft.freemodeevents.events.challenges;

import net.jahcraft.freemodeevents.events.FreemodeEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;

public class RampageEvent extends FreemodeEvent {

    private final int timeLimit;

    private final HashMap<Player, Integer> kills;

    public RampageEvent() {
        this(Main.config.getConfig().getInt("rampage-timer"));
    }

    public RampageEvent(int timeLimit) {
        this.timeLimit = timeLimit;
        kills = new HashMap<>();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!Main.plugin.isRunningEvent(this)) return;
        if (!event.getEntity().isDead()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getEntity() instanceof Mob)) return;

        Player player = (Player) event.getDamager();

        if (!kills.containsKey(player)) kills.put(player, 1);
        else kills.put(player, kills.get(player) + 1);

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

        Main.plugin.finishEvent(this);

    }
}
