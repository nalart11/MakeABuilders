package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.chat.TagFormatter;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

public class ReplyCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("reply", "r")
                        .senderType(Player.class)
                        .required("text", StringParser.greedyStringParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("text")))
        );
    }

    private void handle(@NotNull Player sender, String message) {

        Player target = MakeABuilders.instance.getLastMessaged(sender);

        String senderName = ChatUtils.getFormattedPlayerString(sender.getName(), true);
        String senderFooter = ChatUtils.getFormattedPlayerString(sender.getName(), false);

        String targetName = ChatUtils.getFormattedPlayerString(target.getName(), true);

        String formattedMessage = TagFormatter.format(message);

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