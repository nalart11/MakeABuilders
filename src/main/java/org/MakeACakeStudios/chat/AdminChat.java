package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class AdminChat {

    private static final Set<Player> adminChatPlayers = new HashSet<>();

    private static boolean isAdmin(Player player) {
        String role = PlayerDataStorage.instance.getPlayerRoleByName(player.getName());
        return role.equals("owner") || role.equals("admin") || role.equals("moderator") || role.equals("developer");
    }

    public static void toggleAdminChat(Player player) {
        if (adminChatPlayers.contains(player)) {
            removeFromAdminChat(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вы покинули чат <red>администрации.</red></green>"));
        } else {
            adminChatPlayers.add(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вы вошли в чат <red>администрации.</red></green>"));
        }
    }

    public static void removeFromAdminChat(Player player) {
        adminChatPlayers.remove(player);
    }

    public static boolean isInAdminChat(Player player) {
        return adminChatPlayers.contains(player);
    }

    public static void sendAdminMessage(Player sender, String message) {
        String formattedName = ChatUtils.getFormattedPlayerString(sender.getName(), true);
        message = TagFormatter.format(message);
        message = ChatUtils.replaceLocationTag(sender, message);
        String formattedMessage = "<hover:show_text:'<white><red>Admin Chat</red>'><red>[AC]</red></hover> " + formattedName + " > " + message;
        Component component = MiniMessage.miniMessage().deserialize(formattedMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isAdmin(player)) {
                player.sendMessage(component);
            }
        }
    }
}
