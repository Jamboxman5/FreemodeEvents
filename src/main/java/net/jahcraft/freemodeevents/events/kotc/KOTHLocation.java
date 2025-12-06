package net.jahcraft.freemodeevents.events.kotc;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class KOTHLocation {
    final int radius;
    final Location center;
    final KothShape shape;

    public KOTHLocation(int radius, Location loc, KothShape shape) {
        this.radius = radius;
        this.center = loc;
        this.shape = shape;
    }

    public enum KothShape { ROUND, SQUARE }

    public static KOTHLocation pullFromConfig() {
        FileConfiguration config = Main.plugin.getConfig();

        ConfigurationSection section =
                config.getConfigurationSection("castle-defend-locations");

        if (section == null) {
            Main.plugin.getLogger().warning("No castle-defend-locations defined!");
            return null;
        }

        Map<String, KOTHLocation> locations = new HashMap<>();

        for (String key : section.getKeys(false)) {
            ConfigurationSection loc = section.getConfigurationSection(key);
            if (loc == null) continue;

            String shapeStr = loc.getString("shape", "square").toUpperCase();
            String world = loc.getString("world", "world");
            KOTHLocation.KothShape shape;

            try {
                shape = KOTHLocation.KothShape.valueOf(shapeStr);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid shape at " + key + ": " + shapeStr);
                continue;
            }

            int radius = loc.getInt("radius");
            double x = loc.getDouble("centerX");
            double y = loc.getDouble("centerY");
            double z = loc.getDouble("centerZ");

            locations.put(key,
                    new KOTHLocation(radius, new Location(Bukkit.getWorld(world), x, y, z), shape)
            );
        }

        return locations.values().stream().findFirst().orElse(null);
    }
}

