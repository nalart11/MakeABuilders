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
        return false;
    }

    private void sendFormattedMessageWithIndices(Player player, List<String[]> messages, int currentIndex) {
        String[] selectedMessage = messages.get(currentIndex);
        sendFormattedMessage(player, selectedMessage);

        int messageCount = messages.size();
        if (messageCount > 1) { // Панель с индексами выводится только если сообщений больше одного
            StringBuilder indices = new StringBuilder();

            if (currentIndex <= 1) {
                // Для первых двух сообщений показываем [1] [2] [3] ... [x]
                for (int i = 1; i <= Math.min(3, messageCount); i++) {
                    if (i - 1 == currentIndex) {
                        indices.append("<red>[")
                                .append(i)
                                .append("]</red> ");
                    } else {
                        indices.append("<click:run_command:/mailread ")
                                .append(i)
                                .append("><green>[")
                                .append(i)
                                .append("]</green></click> ");
                    }
                }
                if (messageCount > 3) {
                    indices.append("... ");
                    indices.append("<click:run_command:/mailread ")
                            .append(messageCount)
                            .append("><green>[")
                            .append(messageCount)
                            .append("]</green></click> ");
                }
            } else if (currentIndex < messageCount - 1) {
                // Для сообщений от 3-го до предпоследнего показываем [currentIndex - 1] [currentIndex] [currentIndex + 1] ... [x]
                indices.append("<click:run_command:/mailread ")
                        .append(currentIndex)
                        .append("><green>[")
                        .append(currentIndex)
                        .append("]</green></click> ");
                indices.append("<red>[")
                        .append(currentIndex + 1)
                        .append("]</red> ");
                indices.append("<click:run_command:/mailread ")
                        .append(currentIndex + 2)
                        .append("><green>[")
                        .append(currentIndex + 2)
                        .append("]</green></click> ");
                if (currentIndex + 1 < messageCount - 1) {
                    // Добавляем многоточие и последний индекс, если сообщений больше чем [currentIndex + 1]
                    indices.append("... ");
                    indices.append("<click:run_command:/mailread ")
                            .append(messageCount)
                            .append("><green>[")
                            .append(messageCount)
                            .append("]</green></click> ");
                }
            } else {
                // Для последнего сообщения показываем [x-2] [x-1] [x]
                for (int i = messageCount - 2; i <= messageCount; i++) {
                    if (i == currentIndex + 1) {
                        indices.append("<red>[")
                                .append(i)
                                .append("]</red> ");
                    } else {
                        indices.append("<click:run_command:/mailread ")
                                .append(i)
                                .append("><green>[")
                                .append(i)
                                .append("]</green></click> ");
                    }
                }
            }
            player.sendMessage(miniMessage.deserialize(indices.toString()));
        }
    }

    // Метод для форматирования сообщения
    private void sendFormattedMessage(Player player, String[] messageData) {
        String formattedMessage = "<gray>--------------------------</gray>\n" +
                "<green>Отправитель:</green> " + messageData[0] + messageData[1] + messageData[2] + "\n" +
                "<green>Сообщение:</green> " + messageData[3] + "\n" +
                "<gray>--------------------------</gray>";
        player.sendMessage(miniMessage.deserialize(formattedMessage));
    }
}
