package net.jahcraft.freemodeevents.events;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class FreemodeEvent extends BukkitRunnable implements Listener {

    private final String eventName;

    protected FreemodeEvent(String eventName) {
        this.eventName = eventName;
    }

    public abstract void finish();

    public String getName() { return eventName; }

}
