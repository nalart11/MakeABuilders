package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.other.MailUtils;
import org.MakeACakeStudios.storage.MailStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MailReadCommand implements Command{
    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("mailread")
                        .senderType(Player.class)
                        .required("id", IntegerParser.integerParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("id")))
                        .build()
        );
    }

    private void handle(@NotNull Player sender, Integer messageIndex) {
        List<String[]> messages = MailStorage.instance.getMessages(sender.getName());

        messageIndex--;
        if (messageIndex >= 0 && messageIndex < messages.size()) {
            String[] selectedMessage = messages.get(messageIndex);
            long messageId = Long.parseLong(selectedMessage[0]);

            MailStorage.instance.markMessageAsRead(messageId);

            MailUtils.instance.sendFormattedMessageWithIndices(sender, messages, messageIndex);
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Сообщение с таким номером не найдено.</red>"));
        }
    }
}
