package net.jahcraft.freemodeevents.config;

import net.jahcraft.freemodeevents.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;

public class LeaderboardManager {

    private FileConfiguration monthlyBoard = null;
    private FileConfiguration globalBoard = null;
    private File monthlyLeaderboardFile = null;
    private File globalLeaderboardFile = null;
    private final File dataFolder;
    private final File monthlyFolder;

    public LeaderboardManager() {

        // saves/initializes the config
        dataFolder = new File(Main.plugin.getDataFolder(), "leaderboards");
        monthlyFolder = new File(dataFolder, "monthly");
        generateNewMonthFile();

    }

    public void reloadConfig() {
        if (this.monthlyLeaderboardFile == null) generateNewMonthFile();
        if (this.globalLeaderboardFile == null) generateNewGlobalFile();

        this.monthlyBoard = YamlConfiguration.loadConfiguration(this.monthlyLeaderboardFile);
        this.globalBoard = YamlConfiguration.loadConfiguration(this.globalLeaderboardFile);

    }

    public FileConfiguration getGlobalBoard() {
        if (this.globalBoard == null)
            reloadConfig();

        return this.globalBoard;
    }

    public FileConfiguration getMonthlyBoard() {
        if (this.monthlyBoard == null)
            reloadConfig();

        return this.monthlyBoard;
    }

    public void saveConfig() {
        if (this.monthlyBoard == null || this.monthlyLeaderboardFile == null ||
            this.globalBoard == null || this.globalLeaderboardFile == null)
            return;

        try {
            this.getMonthlyBoard().save(this.monthlyLeaderboardFile);
        } catch (IOException e) {
            Main.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.monthlyLeaderboardFile, e);
        }

        try {
            this.getGlobalBoard().save(this.globalLeaderboardFile);
        } catch (IOException e) {
            Main.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.globalLeaderboardFile, e);
        }
    }

    public void generateNewMonthFile() {
        boolean directoryMade = true;
        if (!monthlyFolder.exists()) directoryMade = monthlyFolder.mkdirs();
        if (!directoryMade) {
            Bukkit.getLogger().warning("Issue making monthly directory!");
            return;
        }
        if (this.monthlyLeaderboardFile == null) {
            LocalDate now = LocalDate.now();
            this.monthlyLeaderboardFile = new File(monthlyFolder, now.getMonth().toString() + "_" + now.getYear() + ".yml");
        }
        boolean leaderboardMade = true;
        if (!this.monthlyLeaderboardFile.exists()) {
            try {
                leaderboardMade = monthlyLeaderboardFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!leaderboardMade) {
                Bukkit.getLogger().warning("Issue making leaderboard file " + monthlyLeaderboardFile.getName());
                return;
            }
        }
    }

    public void generateNewGlobalFile() {
        boolean directoryMade = true;
        if (!dataFolder.exists()) directoryMade = dataFolder.mkdirs();
        if (!directoryMade) {
            Bukkit.getLogger().warning("Issue making leaderboard directory!");
            return;
        }
        if (this.globalLeaderboardFile == null) {
            this.globalLeaderboardFile = new File(dataFolder, "all-time.yml");
        }
        boolean leaderboardMade = true;
        if (!this.globalLeaderboardFile.exists()) {
            try {
                leaderboardMade = globalLeaderboardFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!leaderboardMade) {
                Bukkit.getLogger().warning("Issue making leaderboard file " + monthlyLeaderboardFile.getName());
                return;
            }
        }
    }

}
