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

                if (messages.isEmpty()) {
                    player.sendMessage("У вас нет непрочитанных сообщений.");
                } else {
                    player.sendMessage(miniMessage.deserialize("<yellow>Ваши непрочитанные сообщения:</yellow>"));
                    // Отправляем первое сообщение
                    sendFormattedMessageWithIndices(player, messages, 0);
                }
                return true;
            } else {
                sender.sendMessage("Эта команда доступна только игрокам.");
                return true;
            }
        }

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
                        // Отображаем выбранное сообщение и обновляем индексы
                        sendFormattedMessageWithIndices(player, messages, messageIndex);
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

        if (command.getName().equalsIgnoreCase("maildelete")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length != 1) {
                    player.sendMessage(miniMessage.deserialize("<red>Укажите ID сообщения для удаления: /maildelete <id></red>"));
                    return true;
                }

                try {
                    long messageId = Long.parseLong(args[0]);
                    boolean success = mailStorage.deleteMessageById(messageId);

                    if (success) {
                        player.sendMessage(miniMessage.deserialize("<green>✔ Сообщение удалено.</green>"));
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>Сообщение с таким ID не найдено.</red>"));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(miniMessage.deserialize("<red>Неправильный формат ID сообщения.</red>"));
                }
                return true;
            } else {
                sender.sendMessage("Эта команда доступна только игрокам.");
                return true;
            }
        }

        return false;
    }

    private void sendFormattedMessageWithIndices(Player player, List<String[]> messages, int currentIndex) {
        String[] selectedMessage = messages.get(currentIndex);
        sendFormattedMessage(player, selectedMessage, currentIndex);

        int messageCount = messages.size();
        if (messageCount > 1) { // Панель с индексами выводится только если сообщений больше одного
            StringBuilder indices = new StringBuilder();

            if (currentIndex <= 1) {
                // Для первых двух сообщений показываем [1] [2] [3] ... [x]
                for (int i = 1; i <= Math.min(3, messageCount); i++) {
                    if (i - 1 == currentIndex) {
                        indices.append("<color:#fc8803>[")
                                .append(i)
                                .append("]</color> ");
                    } else {
                        indices.append("<click:run_command:/mailread ")
                                .append(i)
                                .append("><yellow>[")
                                .append(i)
                                .append("]</yellow></click> ");
                    }
                }
                if (messageCount > 3) {
                    indices.append("... ");
                    indices.append("<click:run_command:/mailread ")
                            .append(messageCount)
                            .append("><yellow>[")
                            .append(messageCount)
                            .append("]</yellow></click> ");
                }
            } else if (currentIndex < messageCount - 2) {
                // Для сообщений от 3-го до предпоследнего показываем [1] ... [currentIndex] [currentIndex+1] [currentIndex+2] ... [x]
                indices.append("<click:run_command:/mailread 1><yellow>[1]</yellow></click> ");
                indices.append("... ");
                indices.append("<click:run_command:/mailread ")
                        .append(currentIndex)
                        .append("><yellow>[")
                        .append(currentIndex)
                        .append("]</yellow></click> ");
                indices.append("<color:#fc8803>[")
                        .append(currentIndex + 1)
                        .append("]</color> ");
                indices.append("<click:run_command:/mailread ")
                        .append(currentIndex + 2)
                        .append("><yellow>[")
                        .append(currentIndex + 2)
                        .append("]</yellow></click> ");
                indices.append("... ");
                indices.append("<click:run_command:/mailread ")
                        .append(messageCount)
                        .append("><yellow>[")
                        .append(messageCount)
                        .append("]</yellow></click> ");
            } else {
                // Для последнего и предпоследнего сообщения показываем [1] ... [x-2] [x-1] [x]
                indices.append("<click:run_command:/mailread 1><yellow>[1]</yellow></click> ");
                indices.append("... ");
                for (int i = messageCount - 2; i <= messageCount; i++) {
                    if (i == currentIndex + 1) {
                        indices.append("<color:#fc8803>[")
                                .append(i)
                                .append("]</color> ");
                    } else {
                        indices.append("<click:run_command:/mailread ")
                                .append(i)
                                .append("><yellow>[")
                                .append(i)
                                .append("]</yellow></click> ");
                    }
                }
            }
            player.sendMessage(miniMessage.deserialize(indices.toString()));
        }
    }

    // Метод для форматирования сообщения с кнопками [✔] и [✖]
    private void sendFormattedMessage(Player player, String[] messageData, int currentIndex) {
        String messageId = messageData[0];
        String senderPrefix = messageData[1];
        String sender = messageData[2];
        String senderSuffix = messageData[3];
        String message = messageData[4];

        int nextIndex = currentIndex + 2; // Индекс для следующего сообщения (команда /mailread ожидает 1-based index)
        String formattedMessage = "<gray>--------------------------</gray>\n" +
                "<green>Отправитель:</green> " + senderPrefix + sender + senderSuffix + "\n" +
                "<green>Сообщение:</green> " + message + "\n" +
                "<click:run_command:/mailread " + nextIndex + "><green>[✔]</green></click>  " +
                "<click:run_command:/maildelete " + messageId + "><red>[✖]</red></click>\n" +
                "<gray>--------------------------</gray>";
        player.sendMessage(miniMessage.deserialize(formattedMessage));
    }
}
