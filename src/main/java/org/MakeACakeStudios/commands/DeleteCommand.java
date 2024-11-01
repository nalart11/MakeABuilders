package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.chat.ChatHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand implements CommandExecutor {

    private final ChatHandler chatHandler;

    public DeleteCommand(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /delete <id>");
            return false;
        }

        try {
            int messageId = Integer.parseInt(args[0]);
            chatHandler.deleteMessage(messageId);
            sender.sendMessage("Сообщение успешно удалено.");
        } catch (NumberFormatException e) {
            sender.sendMessage("ID сообщения должен быть числом.");
        }

        return true;
    }
}
