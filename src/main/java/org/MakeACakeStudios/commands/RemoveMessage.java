package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.chat.ChatHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RemoveMessage implements CommandExecutor {

    private final ChatHandler chatHandler;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RemoveMessage(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Использование: /delete <id><red>"));
            return false;
        }

        try {
            int messageId = Integer.parseInt(args[0]);
            chatHandler.deleteMessage(messageId);
            sender.sendMessage(miniMessage.deserialize("<green>Сообщение успешно удалено.</green>"));
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>ID сообщения должен быть числом.</red>"));
        }

        return true;
    }
}
