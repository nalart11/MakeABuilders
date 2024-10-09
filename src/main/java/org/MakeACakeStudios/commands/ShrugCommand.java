package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.storage.*;

public class ShrugCommand implements CommandExecutor {

    private final PlayerNameStorage playerNameStorage;
    private final MiniMessage miniMessage;

    public ShrugCommand(PlayerNameStorage playerNameStorage, MiniMessage miniMessage) {
        this.playerNameStorage = playerNameStorage;
        this.miniMessage = miniMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getDisplayName();
            System.out.println(playerName + " → " + "¯\\_(ツ)_/¯");
            String prefix = playerNameStorage.getPlayerPrefix(player);
            String suffix = playerNameStorage.getPlayerSuffix(player);

            String finalMessage = prefix + playerName + suffix + " > " + "¯\\_(ツ)_/¯";
            Component parsedMessage = miniMessage.deserialize(finalMessage);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(parsedMessage);
            }
        } else {
            sender.sendMessage("¯\\_(ツ)_/¯");
        }
        return true;
    }
}
