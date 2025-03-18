package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.parsers.AsyncPlayerParser;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;


public class MessageCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("message", "msg")
                        .senderType(Player.class)
                        .required("player", AsyncPlayerParser.asyncPlayerParser())
                        .required("text", StringParser.greedyStringParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player"), ctx.get("text")))
        );
    }

    private void handle(@NotNull Player sender, @NotNull Player target, String message) {

        String senderName = ChatUtils.getFormattedPlayerString(sender.getName(), true);
        String senderFooter = ChatUtils.getFormattedPlayerString(sender.getName(), false);

        String targetName = ChatUtils.getFormattedPlayerString(target.getName(), true);


        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не найден!</red>"));
            return;
        }

        String formattedMessage = TagFormatter.format(message);
        formattedMessage = ChatUtils.replaceLocationTag(sender, formattedMessage);

        String senderMessage = "<green>Вы</green> <yellow>→</yellow> "
                + targetName + ": <gray>" + formattedMessage + "</gray>";
        String targetMessage = "<click:suggest_command:'/msg " + sender.getName() + " '>"
                + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы ответить игроку "
                + senderFooter + ".'>" + senderName + "</hover></click> <yellow>→</yellow> <green>Вы</green>: <gray>" + formattedMessage + "</gray>";

        sender.sendMessage(MiniMessage.miniMessage().deserialize(senderMessage));
        target.sendMessage(MiniMessage.miniMessage().deserialize(targetMessage));
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

        MakeABuilders.instance.setLastMessaged(sender, target);
    }
}