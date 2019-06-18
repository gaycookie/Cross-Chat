package moe.kawaaii.cross_chat.events;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static moe.kawaaii.cross_chat.mainClass.config;

public class onDiscordMessage implements MessageCreateListener {

    private ChatColor closestColor(Color roleColor) {
        // Color converting function done by SimonOrJ (https://github.com/SimonOrJ) //
        // https://github.com/ArcaneMinecraft/ArcaneBungee/blob/master/src/main/java/com/arcaneminecraft/bungee/module/MessengerModule.java#L153 //
        int r = roleColor.getRed();
        int g = roleColor.getGreen();
        int b = roleColor.getBlue();
        int cc = 0;

        if (r >= 0x55 && g >= 0x55 && b >= 0x55) {
            cc += 1 << 3;
            if (r >= 0xAA)
                cc += 1 << 2;
            if (g >= 0xAA)
                cc += 1 << 1;
            if (b >= 0xAA)
                cc += 1;
        } else {
            if (r >= 0x55)
                cc += 1 << 2;
            if (g >= 0x55)
                cc += 1 << 1;
            if (b >= 0x55)
                cc += 1;
        }
        // End of color converting function //
        return ChatColor.getByChar(Integer.toHexString(cc).charAt(0));
    }

    private String formatPlaceholders(Server server, User user, String message) {
        String format = config.getString("config.message-formats.minecraft");
        if (format == null) format = "[Discord] &l{username}&r: {message}";

        String usernameString = user.getName();
        if (config.getBoolean("config.discord.inherit_role_color")) {
            if (server.getRoleColor(user).isPresent()) {
                usernameString = closestColor(server.getRoleColor(user).get()) + user.getName() + ChatColor.RESET;
            }
        }

        Map<String, String> values = new HashMap<>();
        values.put("username", usernameString);
        values.put("user_id", user.getIdAsString());
        values.put("message", message);

        StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
        return sub.replace(ChatColor.translateAlternateColorCodes('&', format));
    }

    @Override
    public void onMessageCreate(MessageCreateEvent e) {
        if (!e.getServer().isPresent()) return;
        Server server = e.getServer().get();

        TextChannel channel = e.getChannel();
        if (!channel.getIdAsString().equalsIgnoreCase(config.getString("config.discord.channels.discord-to-minecraft"))) return;

        Optional<User> userAuthor = e.getMessage().getUserAuthor();
        if (!userAuthor.isPresent()) return;
        User author = userAuthor.get();

        if (author.isBot()) return;

        if (config.getString("config.discord.required_role") != null) {
            if (server.getRoleById(config.getString("config.discord.required_role")).isPresent()) {
                Role role = server.getRoleById(config.getString("config.discord.required_role")).get();
                if (!role.getUsers().contains(author)) return;
            }
        }

        String content = formatPlaceholders(server, author, e.getMessage().getReadableContent());
        Bukkit.getServer().broadcastMessage(content);
    }
}
