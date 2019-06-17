package moe.kawaaii.cross_chat.events;

import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.javacord.api.entity.channel.TextChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static moe.kawaaii.cross_chat.mainClass.config;
import static moe.kawaaii.cross_chat.mainClass.discord_api;

public class onMinecraftMessage implements Listener {

    private String formatPlaceholders(Player player, String message) {
        String format = config.getString("config.message-formats.discord");
        if (format == null) format = "[**{username}**]: {message}";
        String parsed = PlaceholderAPI.setBracketPlaceholders(player, format);

        Map<String, String> values = new HashMap<>();
        values.put("username", player.getName());
        values.put("user_id", player.getUniqueId().toString());
        values.put("message", message);
        values.put("luckpermsRank", StringUtils.capitalize(PlaceholderAPI.setPlaceholders(player, "%luckperms_lowest_group_by_weight%"))); // Special for Kawaaii Survival
        values.put("mcmmoPowerlevel", PlaceholderAPI.setPlaceholders(player, "%mcmmo_power_level%")); // Special for Kawaaii Survival

        StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
        return sub.replace(parsed);
    }

    @EventHandler
    public void onMessageCreate(AsyncPlayerChatEvent e) {
        Optional<TextChannel> chatRoomChannel = discord_api.getTextChannelById(config.getString("config.discord.channels.minecraft-to-discord"));
        if (chatRoomChannel.isPresent()) {
            TextChannel channel = chatRoomChannel.get();
            String content = formatPlaceholders(e.getPlayer(), e.getMessage());
            channel.sendMessage(content);
        }
    }

}
