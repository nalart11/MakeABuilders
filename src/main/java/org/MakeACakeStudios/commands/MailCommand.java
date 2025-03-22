package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.parsers.AsyncOfflinePlayerParser;
import org.MakeACakeStudios.storage.MailStorage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MailCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("mail")
                        .senderType(Player.class)
                        .required("player", AsyncOfflinePlayerParser.asyncOfflinePlayerParser())
                        .required("message", StringParser.greedyStringParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player"), ctx.get("message")))
                        .build()
        );
    }

    private void handle(@NotNull Player sender, @NotNull OfflinePlayer target, String message) {
        if (!PlayerDataStorage.instance.playerExistsInDatabase(target.getName())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не найден в базе данных.</red>"));
            return;
        }

        message = TagFormatter.format(message);
        message = ChatUtils.replaceLocationTag(sender, message);

        String senderName = sender.getName();
        MailStorage.instance.addMessage(target.getName(), senderName, message);

        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>✔ Сообщение отправлено.</green>"));

        Player targetPlayer = Bukkit.getPlayer(target.getUniqueId());
        if (targetPlayer != null) {
            List<String[]> messages = MailStorage.instance.getMessages(targetPlayer.getName());
            int lastId = messages.size();
            String formattedSender = ChatUtils.getFormattedPlayerString(sender.getName(), true); // Добавляем оформление только при отображении
            targetPlayer.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<green><click:run_command:/mailread " + lastId +
                            "><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы прочитать последнее сообщение от " + formattedSender + ".'>У вас есть новое сообщение!</click></green>"
            ));
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        }
    }
}