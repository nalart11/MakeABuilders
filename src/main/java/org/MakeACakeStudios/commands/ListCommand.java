package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.PlayerNameStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ListCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlayerNameStorage playerNameStorage;

    public ListCommand(MakeABuilders plugin, PlayerNameStorage playerNameStorage) {
        this.plugin = plugin;
        this.playerNameStorage = playerNameStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StringBuilder playerList = new StringBuilder("<yellow>Игроки онлайн:</yellow>\n");

        for (Player player : Bukkit.getOnlinePlayers()) {
            String prefix = playerNameStorage.getPlayerPrefix(player);
            String suffix = playerNameStorage.getPlayerSuffix(player);
            String playerName = player.getName();

            // Format the player entry with prefix, name, and suffix
            playerList.append("<green>")
                    .append(prefix).append(playerName).append(suffix)
                    .append("</green>, ");
        }

        // Remove the trailing comma and space if players are listed
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            playerList.setLength(playerList.length() - 2);  // Remove last ", "
        }

        sender.sendMessage(miniMessage.deserialize(playerList.toString()));
        return true;
    }
}
