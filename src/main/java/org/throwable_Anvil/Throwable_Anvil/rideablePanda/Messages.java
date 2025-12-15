package org.throwable_Anvil.Throwable_Anvil.rideablePanda;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {
    private final FileConfiguration config;

    public Messages(FileConfiguration config) {
        this.config = config;
    }

    public String get(String path) {
        return colorize(config.getString("messages." + path, "&cMessage not found: " + path));
    }

    public String get(String path, String placeholder, String value) {
        return colorize(config.getString("messages." + path, "&cMessage not found: " + path)
                .replace("{" + placeholder + "}", value));
    }

    public String get(String path, String placeholder1, String value1, String placeholder2, String value2) {
        return colorize(config.getString("messages." + path, "&cMessage not found: " + path)
                .replace("{" + placeholder1 + "}", value1)
                .replace("{" + placeholder2 + "}", value2));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
