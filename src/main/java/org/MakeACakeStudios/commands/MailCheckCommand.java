package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.other.MailUtils;
import org.MakeACakeStudios.storage.MailStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MailCheckCommand implements Command {
    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("mailcheck")
                        .senderType(Player.class)
                        .handler(ctx -> handle(ctx.sender()))
                        .build()
        );
    }

    private void handle(@NotNull Player sender) {
        List<String[]> messages = MailStorage.instance.getMessages(sender.getName());
        if (messages.isEmpty()) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нет сообщений.</red>"));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Ваши сообщения:</yellow>"));

                    MailUtils.instance.sendFormattedMessageWithIndices(sender, messages, 0);

                    String[] firstMessage = messages.get(0);
                    long messageId = Long.parseLong(firstMessage[0]);
                    MailStorage.instance.markMessageAsRead(messageId);
                }
    }
}
