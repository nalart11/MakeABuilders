package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.storage.MailStorage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Arrays;

public class MailCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final MailStorage mailStorage;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MailCommand(MakeABuilders plugin) {
        this.plugin = plugin;
        this.mailStorage = new MailStorage(plugin.getConnection());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mail")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length < 2) {
                    player.sendMessage(miniMessage.deserialize("<red>Используйте: /mail <имя_игрока> <сообщение></red>"));
                    return true;
                }

                String recipientName = args[0];
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                // Добавляем сообщение в базу данных
                mailStorage.addMessage(recipientName, plugin.getPlayerPrefix(player), player.getName(), plugin.getPlayerSuffix(player), message);
                player.sendMessage(miniMessage.deserialize("<green>✔ Сообщение отправлено.</green>"));
                return true;
            } else {
                sender.sendMessage("Эта команда доступна только игрокам.");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("mailcheck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<String[]> messages = mailStorage.getMessages(player.getName());
                int messageCount = messages.size();

                if (messages.isEmpty()) {
                    player.sendMessage("У вас нет непрочитанных сообщений.");
                } else {
                    player.sendMessage(miniMessage.deserialize("<yellow>Ваши непрочитанные сообщения:</yellow>"));

                    // Отправляем сообщение для первого письма (по умолчанию)
                    String[] firstMessage = messages.get(0);
                    sendFormattedMessage(player, firstMessage);

                    // Создаем строку с кликабельными индексами сообщений
                    StringBuilder indices = new StringBuilder();
                    for (int i = 1; i <= messageCount; i++) {
                        if (i <= 3 || i == messageCount) {
                            // Добавляем кликабельный индекс
                            indices.append("<click:run_command:/mailread ")
                                    .append(i)
                                    .append("><green>[")
                                    .append(i)
                                    .append("]</green></click> ");
                        }
                        if (i == 3 && messageCount > 3) {
                            // Если сообщений больше 3, добавляем многоточие перед последним индексом
                            indices.append("... ");
                        }
                    }
                    player.sendMessage(miniMessage.deserialize(indices.toString()));

                    // Сообщения не очищаются до тех пор, пока игрок не прочтет их
                }
                return true;
            } else {
                sender.sendMessage("Эта команда доступна только игрокам.");
                return true;
            }
        }

// Дополнительная команда для обработки кликов по индексам
        if (command.getName().equalsIgnoreCase("mailread")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length != 1) {
                    player.sendMessage(miniMessage.deserialize("<red>Укажите номер сообщения для чтения: /mailread <номер></red>"));
                    return true;
                }

                try {
                    int messageIndex = Integer.parseInt(args[0]) - 1;
                    List<String[]> messages = mailStorage.getMessages(player.getName());

                    if (messageIndex >= 0 && messageIndex < messages.size()) {
                        // Отображаем сообщение по индексу
                        String[] selectedMessage = messages.get(messageIndex);
                        sendFormattedMessage(player, selectedMessage);
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>Сообщение с таким номером не найдено.</red>"));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(miniMessage.deserialize("<red>Неправильный формат номера сообщения.</red>"));
                }
                return true;
            }
            return false;
        }
        return false;
    }
    private void sendFormattedMessage(Player player, String[] messageData) {
        String formattedMessage = "<gray>--------------------------</gray>\n" +
                "<green>Отправитель:</green> " + messageData[0] + messageData[1] + messageData[2] + "\n" +
                "<green>Сообщение:</green> " + messageData[3] + "\n" +
                "<gray>--------------------------</gray>";
        player.sendMessage(miniMessage.deserialize(formattedMessage));
    }
}