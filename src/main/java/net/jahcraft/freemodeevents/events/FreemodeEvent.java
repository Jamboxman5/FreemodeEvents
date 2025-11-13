package net.jahcraft.freemodeevents.events;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class FreemodeEvent extends BukkitRunnable implements Listener {

    public abstract void finish();

}
