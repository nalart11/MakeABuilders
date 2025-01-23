package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.storage.PlayerDataStorage;
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

        String senderPrefix = PlayerDataStorage.instance.getPlayerPrefixByName(sender.getName());
        String senderSuffix = PlayerDataStorage.instance.getPlayerSuffixByName(sender.getName());
        String senderName = senderPrefix + sender.getName() + senderSuffix;

        String targetPrefix = PlayerDataStorage.instance.getPlayerPrefixByName(target.getName());
        String targetSuffix = PlayerDataStorage.instance.getPlayerSuffixByName(target.getName());
        String targetName = targetPrefix + target.getName() + targetSuffix;

        String formattedMessage = TagFormatter.format(message);

        String senderMessage = "<green>Вы</green> <yellow>→</yellow> "
                + targetName + ": <gray>" + formattedMessage + "</gray>";
        String targetMessage = "<click:suggest_command:'/msg " + sender.getName() + " '>"
                + "<hover:show_text:'Нажм ите <green>ЛКМ</green>, чтобы ответить игроку "
                + senderName + ".'>" + senderName + "</hover></click> <yellow>→</yellow> <green>Вы</green>: <gray>" + formattedMessage + "</gray>";

        sender.sendMessage(MiniMessage.miniMessage().deserialize(senderMessage));
        target.sendMessage(MiniMessage.miniMessage().deserialize(targetMessage));

        MakeABuilders.instance.setLastMessaged(sender, target);
    }
}