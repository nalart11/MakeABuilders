package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.storage.*;

public class SmugCommand implements CommandExecutor {

    private final PlayerNameStorage playerNameStorage;
    private final MiniMessage miniMessage;

    public SmugCommand(PlayerNameStorage playerNameStorage, MiniMessage miniMessage) {
        this.playerNameStorage = playerNameStorage;
        this.miniMessage = miniMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getDisplayName();
            String prefix = playerNameStorage.getPlayerPrefix(player);
            String suffix = playerNameStorage.getPlayerSuffix(player);

            if (command.getName().equalsIgnoreCase("shrug")) {
                String shrugMessage = "¯\\_(ツ)_/¯";
                sendFormattedMessage(playerName, prefix, suffix, shrugMessage);
            }

            if (command.getName().equalsIgnoreCase("tableflip")) {
                String tableFlipMessage = "<red>(╯°□°)╯︵ ┻━┻</red>";
                sendFormattedMessage(playerName, prefix, suffix, tableFlipMessage);
            }

            if (command.getName().equalsIgnoreCase("unflip")) {
                String unFlipMessage = "┬─┬ノ( º _ ºノ)";
                sendFormattedMessage(playerName, prefix, suffix, unFlipMessage);
            }
        } else {
            if (command.getName().equalsIgnoreCase("shrug")) {
                sender.sendMessage("¯\\_(ツ)_/¯");
            } else if (command.getName().equalsIgnoreCase("tableflip")) {
                sender.sendMessage("(╯°□°)╯︵ ┻━┻");
            } else if (command.getName().equalsIgnoreCase("unflip")) {
                sender.sendMessage("┬─┬ノ( º _ ºノ)");
            }
        }
        return true;
    }

    private void sendFormattedMessage(String playerName, String prefix, String suffix, String emoticon) {
        String finalMessage = prefix + playerName + suffix + " > " + emoticon;
        Component parsedMessage = miniMessage.deserialize(finalMessage);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(parsedMessage);
        }
        System.out.println(playerName + " → " + emoticon);
    }
}