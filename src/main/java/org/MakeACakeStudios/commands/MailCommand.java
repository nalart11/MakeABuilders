package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.storage.MailStorage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
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
    private final PlayerDataStorage playerDataStorage;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final TagFormatter tagFormatter;

    public MailCommand(MakeABuilders plugin, PlayerDataStorage playerDataStorage, TagFormatter tagFormatter) {
        this.plugin = plugin;
        this.mailStorage = plugin.getMailStorage();
        this.playerDataStorage = playerDataStorage;
        this.tagFormatter = tagFormatter;
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

                if (!playerDataStorage.playerExistsInDatabase(recipientName)) {
                    player.sendMessage(miniMessage.deserialize("<red>Игрок не найден в базе данных.</red>"));
                    return true;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                message = tagFormatter.format(message, player);

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
                    player.sendMessage("У вас нет сообщений.");
                } else {
                    player.sendMessage(miniMessage.deserialize("<yellow>Ваши сообщения:</yellow>"));

                    sendFormattedMessageWithIndices(player, messages, 0);

                    String[] firstMessage = messages.get(0);
                    long messageId = Long.parseLong(firstMessage[0]);
                    mailStorage.markMessageAsRead(messageId);
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
                        String[] selectedMessage = messages.get(messageIndex);
                        long messageId = Long.parseLong(selectedMessage[0]);

                        mailStorage.markMessageAsRead(messageId);

                        sendFormattedMessageWithIndices(player, messages, messageIndex);
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>Сообщение с таким номером не найдено.</red>"));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(miniMessage.deserialize("<red>Неправильный формат номера сообщения.</red>"));
                }
                return true;
            }
        }

        return false;
    }

    private void sendFormattedMessageWithIndices(Player player, List<String[]> messages, int currentIndex) {
        String[] selectedMessage = messages.get(currentIndex);
        sendFormattedMessage(player, selectedMessage, currentIndex);

        int messageCount = messages.size();
        if (messageCount > 1) {
            StringBuilder indices = new StringBuilder();

            if (messageCount == 3 || messageCount == 4 || (messageCount == 5 && currentIndex == 2)) {
                for (int i = 1; i <= messageCount; i++) {
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
            } else if (currentIndex <= 1) {
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

    private void sendFormattedMessage(Player player, String[] messageData, int currentIndex) {
        String senderPrefix = messageData[1];
        String sender = messageData[2];
        String senderSuffix = messageData[3];
        String message = messageData[4];
        String status = messageData[5].equals("Прочитано") ? "<gray>[Прочитано]</gray>" : "<yellow>[Непрочитано]</yellow>";

        String formattedMessage = "<gray>--------------------------</gray>\n" +
                "<green>Отправитель:</green> " + senderPrefix + sender + senderSuffix + "\n" +
                "<green>Сообщение:</green> " + message + "\n" +
                status + "\n" +
                "<gray>--------------------------</gray>";
        player.sendMessage(miniMessage.deserialize(formattedMessage));
    }
}
