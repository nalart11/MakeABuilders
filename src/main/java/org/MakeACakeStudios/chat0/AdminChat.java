package org.MakeACakeStudios.chat0;

import net.kyori.adventure.text.Component;
import org.MakeACakeStudios.player.NicknameBuilder;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.MakeACakeStudios.utils.Formatter.*;
import static org.MakeACakeStudios.utils.Formatter.single;

public class AdminChat {

    public static final Set<UUID> players = new LinkedHashSet<>();

    public static boolean isAdmin(@NotNull Player player) {
        String role = PlayerDataStorage.instance.getPlayerRoleByName(player.getName());
        return role.equals("owner") || role.equals("admin") || role.equals("moderator") || role.equals("developer");
    }

    public static Collection<? extends Player> collectOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream().filter(player -> players.contains(player.getUniqueId())).toList();
    }
    
    public static void toggle(@NotNull Player player) {
        toggle(player.getUniqueId());
    }
    
    public static void toggle(@NotNull UUID playerUUID) {
        var player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        if (players.contains(playerUUID)) {
            players.remove(playerUUID);
            player.sendMessage(green("Вы покинули чат ", red("администраци"), "."));
        } else {
            players.add(playerUUID);
            player.sendMessage(green("Вы покинули чат ", red("администраци"), "."));
        }
    }
    
    public static boolean isUsing(@NotNull Player player) {
        return isUsing(player.getUniqueId());
    }
    
    public static boolean isUsing(@NotNull UUID playerUUID) {
        return players.contains(playerUUID);
    }

    public static @NotNull Component formatMessage(@NotNull OfflinePlayer player, @NotNull String message) {
        var prefix = hover(red("[AC]"), red("Admin Chat"));
        var displayName = NicknameBuilder.displayName(player, true, true);
        return single(prefix, Component.space(), displayName, text(" > " + message));
    }
    
}
