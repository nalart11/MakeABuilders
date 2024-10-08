package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Arrays;

public class MailCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MakeABuilders plugin;

    public MailCommand(MakeABuilders plugin) {
        this.plugin = plugin;
    }

    // Метод для обработки команд
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mail")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length < 2) {
                    player.sendMessage("Используйте: /mail <имя_игрока> <сообщение>");
                    return true;
                }

                String recipientName = args[0];
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                // Получаем префикс и суффикс отправителя
                String senderPrefix = plugin.getPlayerPrefix(player);
                String senderSuffix = plugin.getPlayerSuffix(player);

                // Ищем игрока по имени
                Player recipientPlayer = Bukkit.getPlayer(recipientName);

                String recipientPrefix;
                String recipientSuffix;

                if (recipientPlayer != null) {
                    // Игрок в сети, получаем префикс и суффикс напрямую
                    recipientPrefix = plugin.getPlayerPrefix(recipientPlayer);
                    recipientSuffix = plugin.getPlayerSuffix(recipientPlayer);
                } else {
                    // Игрок не в сети, получаем префикс и суффикс из хранилища
                    recipientPrefix = plugin.getPlayerNameStorage().getPlayerPrefixByName(recipientName);
                    recipientSuffix = plugin.getPlayerNameStorage().getPlayerSuffixByName(recipientName);
                }

                // Отправка сообщения с префиксом отправителя
                plugin.getMailStorage().addMessage(recipientName, senderPrefix, player.getName(), senderSuffix, message);
                player.sendMessage(miniMessage.deserialize("<green>Сообщение отправлено игроку " + recipientPrefix + recipientName + recipientSuffix + "</green>."));
                return true;
            } else {
                sender.sendMessage("Эта команда доступна только игрокам.");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("mailcheck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<String[]> playerMessages = plugin.getMailStorage().getMessages(player.getName());

                if (playerMessages.isEmpty()) {
                    player.sendMessage("У вас нет непрочитанных сообщений.");
                    return true;
                }

                player.sendMessage(miniMessage.deserialize("<yellow>Ваши непрочитанные сообщения:</yellow>"));
                player.sendMessage("");
                for (String[] messageData : playerMessages) {
                    String senderPrefix = messageData[0]; // Получаем префикс отправителя
                    String senderName = messageData[1];   // Получаем имя отправителя
                    String senderSuffix = messageData[2];
                    String message = messageData[3];      // Получаем само сообщение

                    // Форматируем и выводим сообщение, используя префикс отправителя
                    player.sendMessage(miniMessage.deserialize("<gray><------------------></gray>"));
                    player.sendMessage(miniMessage.deserialize("<yellow>Отправитель:</yellow> " + senderPrefix + senderName + senderSuffix));
                    player.sendMessage(miniMessage.deserialize("<green>Сообщение:</green> " + message));
                    player.sendMessage(miniMessage.deserialize("<gray><------------------></gray>"));
                    player.sendMessage("");
                }

                // Удаляем сообщения после прочтения
                plugin.getMailStorage().clearMessages(player.getName());
                return true;
            } else {
                sender.sendMessage("Эта команда доступна только игрокам.");
                return true;
            }
        }
        return false;
    }
}
