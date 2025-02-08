package org.MakeACakeStudios.other;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

public class MailUtils {

    public static final MailUtils instance = new MailUtils();

    public void sendFormattedMessageWithIndices(Player player, List<String[]> messages, int currentIndex) {
        String[] selectedMessage = messages.get(currentIndex);
        sendFormattedMessage(player, selectedMessage);

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
            player.sendMessage(MiniMessage.miniMessage().deserialize(indices.toString()));
        }
    }

    public void sendFormattedMessage(Player player, String[] messageData) {
        String senderName = messageData[1];
        String message = messageData[2];
        String status = messageData[3].equals("Прочитано") ? "<gray>[Прочитано]</gray>" : "<yellow>[Непрочитано]</yellow>";

        String formattedMessage = "<gray>--------------------------</gray>\n" +
                "<green>Отправитель:</green> " + senderName + "\n" +
                "<green>Сообщение:</green> " + message + "\n" +
                status + "\n" +
                "<gray>--------------------------</gray>";
        player.sendMessage(MiniMessage.miniMessage().deserialize(formattedMessage));
    }
}
