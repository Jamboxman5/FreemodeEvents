package net.jahcraft.freemodeevents.config;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class DataManager {

    private FileConfiguration config = null;
    private File configFile = null;

    public DataManager() {

        // saves/initializes the config
        saveDefaultConfig();

    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(Main.plugin.getDataFolder(), "config.yml");

        this.config = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultConfigFile = Main.plugin.getResource("config.yml");

        if (defaultConfigFile != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigFile));
            this.config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.config == null)
            reloadConfig();

        return this.config;
    }

    public void saveConfig() {
        if (this.config == null || this.configFile == null)
            return;

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            Main.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null)
            this.configFile = new File(Main.plugin.getDataFolder(), "config.yml");

        if (!this.configFile.exists()) {
            Main.plugin.saveResource("config.yml", false);
        }
    }

}
