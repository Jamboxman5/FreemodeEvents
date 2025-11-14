package net.jahcraft.freemodeevents.listeners;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (Main.plugin.isRunningEvent() && Main.plugin.getCurrentScoreboard() != null) {
            event.getPlayer().setScoreboard(Main.plugin.getCurrentScoreboard());
        }

    }

}
