package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.storage.*;

public class SmugCommand implements CommandExecutor {

    private final MiniMessage miniMessage;
    private final MakeABuilders plugin;

    public SmugCommand(MakeABuilders makeABuilders ,MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
        this.plugin = makeABuilders;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getDisplayName();
            String prefix = plugin.getPlayerPrefix(player);
            String suffix = plugin.getPlayerSuffix(player);

            String userInput = String.join(" ", args); // Соединяем аргументы в одну строку

            if (command.getName().equalsIgnoreCase("shrug")) {
                String shrugMessage = "¯\\_(ツ)_/¯ " + userInput; // Добавляем аргументы к эмодзи
                sendFormattedMessage(playerName, prefix, suffix, shrugMessage);
            }

            if (command.getName().equalsIgnoreCase("tableflip")) {
                String tableFlipMessage = "<red>(╯°□°)╯︵ ┻━┻</red> " + userInput;
                sendFormattedMessage(playerName, prefix, suffix, tableFlipMessage);
            }

            if (command.getName().equalsIgnoreCase("unflip")) {
                String unFlipMessage = "┬─┬ノ( º _ ºノ) " + userInput;
                sendFormattedMessage(playerName, prefix, suffix, unFlipMessage);
            }
        } else {
            String userInput = String.join(" ", args);

            if (command.getName().equalsIgnoreCase("shrug")) {
                sender.sendMessage("¯\\_(ツ)_/¯ " + userInput);
            } else if (command.getName().equalsIgnoreCase("tableflip")) {
                sender.sendMessage("(╯°□°)╯︵ ┻━┻ " + userInput);
            } else if (command.getName().equalsIgnoreCase("unflip")) {
                sender.sendMessage("┬─┬ノ( º _ ºノ) " + userInput);
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
