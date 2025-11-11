package net.jahcraft.freemodeevents.chat;

import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatEventController extends BukkitRunnable {

    private boolean stopReceived = false;

    @Override
    public void run() {
        Bukkit.getLogger().info("Chat Event Controller running!");

        while (!stopReceived) {
            if (Main.plugin.canRunEvent()) {

                Main.plugin.runEvent(getRandomChatEvent());

            } else {
                try {
                    Thread.sleep(1000 * 60 * 15);
                } catch (InterruptedException e) {
                    stopReceived = true;
                }
            }
        }

    }

    public void stop() { stopReceived = true; }

    private BukkitRunnable getRandomChatEvent() {
        return EventUtil.getGenericEvent();
    }

}
