package net.jahcraft.freemodeevents.commands;

import net.jahcraft.freemodeevents.events.challenges.GravityStrikeEvent;
import net.jahcraft.freemodeevents.events.challenges.KillListEvent;
import net.jahcraft.freemodeevents.events.challenges.RampageEvent;
import net.jahcraft.freemodeevents.events.vip.ExecutiveSearchEvent;
import net.jahcraft.freemodeevents.events.chat.UnscrambleEvent;
import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("events")) return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Available subcommands: ");
            sender.sendMessage(ChatColor.RED + "/events next");
            if (sender.hasPermission("freemodeevents.admin")) {
                sender.sendMessage(ChatColor.RED + "/events trigger");
                sender.sendMessage(ChatColor.RED + "/events cancel");
                sender.sendMessage(ChatColor.RED + "/events reload");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("trigger")) {
            if (!sender.hasPermission("freemodeevents.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                return true;
            }
            if (Main.plugin.isRunningEvent()) {
                sender.sendMessage(ChatColor.RED + "You can't start a new event while one is already running!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Improper usage! Use \"/event trigger <event>\"");
                return true;
            }
            if (!Main.plugin.canRunEvent(true)) return true;

            if (args[1].equalsIgnoreCase("killlist")) Main.plugin.runEvent(new KillListEvent());
            if (args[1].equalsIgnoreCase("gravitystrike")) Main.plugin.runEvent(new GravityStrikeEvent());
            if (args[1].equalsIgnoreCase("rampage")) Main.plugin.runEvent(new RampageEvent());
            if (args[1].equalsIgnoreCase("unscramble")) Main.plugin.runEvent(new UnscrambleEvent());
            if (args[1].equalsIgnoreCase("executivesearch") && sender instanceof Player p) Main.plugin.runEvent(new ExecutiveSearchEvent(p, p.getLocation()));
            sender.sendMessage("New event triggered.");
        }
        else if (args[0].equalsIgnoreCase("cancel")) {
            if (!sender.hasPermission("freemodeevents.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                return true;
            }
            if (!Main.plugin.isRunningEvent()) {
                sender.sendMessage(ChatColor.RED + "There's no active event to cancel!");
                return true;
            }
            Main.plugin.finishEvent(Main.plugin.getRunningEvent());
            sender.sendMessage("Event canceled.");
        }
        else if (args[0].equalsIgnoreCase("next")) {
            if (Main.plugin.isRunningEvent()) {
                sender.sendMessage(ChatColor.RED + "There's an event currently running!");
                return true;
            }
            sender.sendMessage("The next event will begin in ~" + Main.plugin.getEventCooldown() + " seconds");
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            Main.config.reloadConfig();
            Main.plugin.loadConfiguration();
            sender.sendMessage("FreemodeEvents configuration reloaded!");
        }

        return true;
    }
}
