package moe.kawaaii.cross_chat.events;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.HashMap;
import java.util.Map;

import static moe.kawaaii.cross_chat.mainClass.config;

public class onDiscordMessage implements MessageCreateListener {

    private String formatPlaceholders(MessageAuthor user, String message) {

        String format = config.getString("config.message-formats.minecraft");
        if (format == null) format = "[Discord] &l{username}&r: {message}";

        Map<String, String> values = new HashMap<>();
        values.put("username", user.getDisplayName());
        values.put("user_id", user.getIdAsString());
        values.put("message", message);

        StrSubstitutor sub = new StrSubstitutor(values, "{", "}");

        return sub.replace(ChatColor.translateAlternateColorCodes('&', format));
    }

    @Override
    public void onMessageCreate(MessageCreateEvent e) {

        if (!e.getChannel().getIdAsString().equals(config.getString("config.discord.channels.discord-to-minecraft"))) return;
        if (e.getMessage().getAuthor().isBotUser()) return;

        String content = formatPlaceholders(e.getMessage().getAuthor(), e.getMessage().getReadableContent());
        Bukkit.getServer().broadcastMessage(content);

    }
}
