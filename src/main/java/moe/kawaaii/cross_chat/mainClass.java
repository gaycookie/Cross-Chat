package moe.kawaaii.cross_chat;

import moe.kawaaii.cross_chat.events.onDiscordMessage;
import moe.kawaaii.cross_chat.events.onMinecraftMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.File;
import java.io.IOException;

public class mainClass extends JavaPlugin implements Listener {

    private static File configFile;
    public static DiscordApi discord_api = null;
    public static FileConfiguration config;

    @Override
    public void onEnable() {

        getLogger().info("Searching for PlaceholderAPI...");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getPluginManager().registerEvents(this, this);
            getLogger().info("PlaceholderAPI was found, hooking now...");
        } else {
            getLogger().info("PlaceholderAPI was not found, disabling now!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loading up Config files...");
        configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            this.saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (config.getString("config.discord.token").isEmpty()) {
            getLogger().info("Discord Bot token is not provided, disabling now!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (config.getString("config.discord.channels.discord-to-minecraft").isEmpty()) {
            getLogger().info("Discord to Minecraft channel is not provided, disabling now!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (config.getString("config.discord.channels.minecraft-to-discord").isEmpty()) {
            getLogger().info("Minecraft to Discord channel is not provided, disabling now!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Enabling Discord API...");
        discord_api = new DiscordApiBuilder().setToken(config.getString("config.discord.token")).login().join();

        getLogger().info("Registering Listeners...");
        discord_api.addListener(new onDiscordMessage());
        getServer().getPluginManager().registerEvents(new onMinecraftMessage(), this);
        getServer().getPluginManager().registerEvents(new mainEvents(), this);
    }

    @Override
    public void onDisable() {

    }

}
