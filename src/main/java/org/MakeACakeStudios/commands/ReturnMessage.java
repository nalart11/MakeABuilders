package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReturnMessage implements CommandExecutor {

    private final ChatHandler chatHandler;
    private final MakeABuilders plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ReturnMessage(MakeABuilders plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(miniMessage.deserialize("<red>Использование: /retmsg <messageId><red>"));
            return true;
        }

        int messageId;
        try {
            messageId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(miniMessage.deserialize("<red>ID сообщения должен быть числом.<red>"));
            return true;
        }

        boolean success = chatHandler.restoreMessage(messageId);
        if (success) {
            player.sendMessage(miniMessage.deserialize("<green>Сообщение успешно восстановлено.</green>"));
        } else {
            player.sendMessage(miniMessage.deserialize("<yellow>Сообщение с таким ID не найдено или не было удалено.</yellow>"));
        }

        return true;
    }
}
