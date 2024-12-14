package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatHandler;
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
    private final ChatHandler chatHandler;

    public SmugCommand(MakeABuilders makeABuilders , MiniMessage miniMessage, ChatHandler chatHandler) {
        this.miniMessage = miniMessage;
        this.plugin = makeABuilders;
        this.chatHandler = chatHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String userInput = String.join(" ", args); // Соединяем аргументы в одну строку

            if (command.getName().equalsIgnoreCase("shrug")) {
                String shrugMessage = ":shrug: " + userInput; // Добавляем аргументы к эмодзи
                sendFormattedMessage(player, shrugMessage);
            }

            if (command.getName().equalsIgnoreCase("tableflip")) {
                String tableFlipMessage = ":tableflip: " + userInput;
                sendFormattedMessage(player, tableFlipMessage);
            }

            if (command.getName().equalsIgnoreCase("unflip")) {
                String unFlipMessage = ":unflip: " + userInput;
                sendFormattedMessage(player, unFlipMessage);
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

    private void sendFormattedMessage(Player sender, String emoticon) {
        chatHandler.handleExternalCommandMessage(sender, emoticon);
        System.out.println(sender.getName() + " → " + emoticon);
    }
}
