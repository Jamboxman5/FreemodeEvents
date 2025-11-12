package net.jahcraft.freemodeevents.commands;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("event")) return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Available subcommands: ");
            sender.sendMessage(ChatColor.RED + "/event cooldown");
            sender.sendMessage(ChatColor.RED + "/event next");
            if (sender.hasPermission("freemodeevents.admin")) {
                sender.sendMessage(ChatColor.RED + "/event trigger");
                sender.sendMessage(ChatColor.RED + "/event cancel");
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
        }
        else if (args[0].equalsIgnoreCase("cooldown")) {
            if (Main.plugin.isRunningEvent()) {
                sender.sendMessage(ChatColor.RED + "There's an event currently running!");
                return true;
            }
            sender.sendMessage("The next event will begin in ~" + Main.plugin.getEventCooldown() + " seconds");
        }
        else if (args[0].equalsIgnoreCase("next")) {
            if (Main.plugin.isRunningEvent()) {
                sender.sendMessage(ChatColor.RED + "There's an event currently running!");
                return true;
            }
            sender.sendMessage("The next event will begin in ~" + Main.plugin.getEventCooldown() + " seconds");
        }

        return true;
    }
}
