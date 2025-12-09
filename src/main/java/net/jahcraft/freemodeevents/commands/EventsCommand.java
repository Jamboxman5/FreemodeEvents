package net.jahcraft.freemodeevents.commands;

import net.jahcraft.freemodeevents.events.challenges.*;
import net.jahcraft.freemodeevents.events.chat.TriviaEvent;
import net.jahcraft.freemodeevents.events.integrations.BassProsEvent;
import net.jahcraft.freemodeevents.events.integrations.TrapperChallengeEvent;
import net.jahcraft.freemodeevents.events.kotc.KOTHLocation;
import net.jahcraft.freemodeevents.events.kotc.KingOfTheCastleEvent;
import net.jahcraft.freemodeevents.events.vip.ExecutiveSearchEvent;
import net.jahcraft.freemodeevents.events.chat.UnscrambleEvent;
import net.jahcraft.freemodeevents.main.Main;
import net.jahcraft.freemodeevents.util.EventUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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
            if (sender.hasPermission("freemodeevents.start")) {
                sender.sendMessage(ChatColor.RED + "/events start");
            }
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
                sender.sendMessage(ChatColor.RED + "Improper usage! Use \"/event trigger <event/random>\"");
                return true;
            }
            if (!Main.plugin.canRunEvent(true)) return true;

            if (args[1].equalsIgnoreCase("killlist")) Main.plugin.runEvent(new KillListEvent());
            if (args[1].equalsIgnoreCase("gravitystrike")) Main.plugin.runEvent(new GravityStrikeEvent());
            if (args[1].equalsIgnoreCase("rampage")) Main.plugin.runEvent(new RampageEvent());
            if (args[1].equalsIgnoreCase("unscramble")) Main.plugin.runEvent(new UnscrambleEvent());
            if (args[1].equalsIgnoreCase("sniperchallenge")) Main.plugin.runEvent(new SniperChallengeEvent());
            if (args[1].equalsIgnoreCase("executivesearch") && sender instanceof Player p) Main.plugin.runEvent(new ExecutiveSearchEvent(p, p.getLocation()));
            if (args[1].equalsIgnoreCase("trivia")) Main.plugin.runEvent(new TriviaEvent());
            if (args[1].equalsIgnoreCase("timetomine")) Main.plugin.runEvent(new TimeToMineEvent());
            if (args[1].equalsIgnoreCase("kingofthecastle")) {
                if (!(sender instanceof Player p)) return true;
                if (args.length < 3) Main.plugin.runEvent(new KingOfTheCastleEvent(new KOTHLocation(10, p.getLocation(), KOTHLocation.KothShape.SQUARE), Main.plugin.getConfig().getInt("castle-defend-timer")));
                else if (args.length < 4) Main.plugin.runEvent(new KingOfTheCastleEvent(new KOTHLocation(Integer.parseInt(args[2]), p.getLocation(), KOTHLocation.KothShape.SQUARE), Main.plugin.getConfig().getInt("castle-defend-timer")));
                return true;
            }
            if (args[1].equalsIgnoreCase("trapperchallenge")) {
                if (EventUtil.hasWesternHunting()) {
                    Main.plugin.runEvent(new TrapperChallengeEvent());
                } else {
                    sender.sendMessage(ChatColor.RED + "You need to have WesternHunting installed to do that!");
                    return true;
                }
            }
            if (args[1].equalsIgnoreCase("basspros")) {
                if (EventUtil.hasWesternHunting()) {
                    Main.plugin.runEvent(new BassProsEvent());
                } else {
                    sender.sendMessage(ChatColor.RED + "You need to have WesternHunting installed to do that!");
                    return true;
                }
            }
            if (args[1].equalsIgnoreCase("random")) Main.plugin.runEvent(EventUtil.getRandomEvent());
            if (Main.plugin.isRunningEvent()) sender.sendMessage("New event triggered.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("start")) {
            if (!sender.hasPermission("freemodeevents.start")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                return true;
            }
            if (!(sender instanceof Player p)) return true;
            if (Main.plugin.isRunningEvent()) {
                sender.sendMessage(ChatColor.RED + "You can't start a new event while one is already running!");
                return true;
            }
            if (!Main.plugin.canRunEvent(true)) return true;
            if (args.length == 1) {

                TextComponent msg = new TextComponent(TextComponent.fromLegacy("§x§F§F§D§7§0§0Executive Search"));
                msg.setUnderlined(true);
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events start executivesearch"));
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§x§0§0§E§8§F§FClick to start event!")
                ));

                sender.sendMessage(ChatColor.of("#49B3FF") + "Select one of the following available events: (Click to start)");
                sender.spigot().sendMessage(msg);
                return true;
            }
            if (args[1].equalsIgnoreCase("executivesearch")) {
                if (System.currentTimeMillis() - Main.lastExecutiveSearch > Main.plugin.getConfig().getInt("executive-search-cooldown") * 1000F) {
                    Main.plugin.runEvent(new ExecutiveSearchEvent(p, p.getLocation()));
                } else {
                    int cooldown = Main.plugin.getConfig().getInt("executive-search-cooldown");
                    int seconds = Math.toIntExact(cooldown - (int) ((System.currentTimeMillis() - Main.lastExecutiveSearch) / 1000f));
                    sender.sendMessage(ChatColor.RED + "You must wait " + EventUtil.secondsToMinutes(seconds) + " before starting another Executive Search!");
                }
                return true;
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("trivia")) {
            if (!(sender instanceof Player p)) return true;
            if (!Main.plugin.isRunningEvent() || !(Main.plugin.getRunningEvent() instanceof TriviaEvent event)) {
                sender.sendMessage(ChatColor.RED + "No trivia event running!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Please specify an answer!");
                return true;
            }
            event.submitAnswer(p, args[1]);
            return true;
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
            sender.sendMessage("The next event will begin in ~" + EventUtil.secondsToMinutes(Main.plugin.getTimeToNextEvent()));
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            Main.plugin.reloadConfig();
            sender.sendMessage("FreemodeEvents configuration reloaded!");
        }

        return true;
    }
}
