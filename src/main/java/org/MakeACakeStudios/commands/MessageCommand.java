package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

import org.MakeACakeStudios.commands.MessageSoundCommand.Sounds;


public class MessageCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("message", "msg")
                        .senderType(Player.class)
                        .required("player", OfflinePlayerParser.offlinePlayerParser())
                        .required("text", StringParser.greedyStringParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player"), ctx.get("text")))
        );
    }

    private void handle(@NotNull Player sender, @NotNull OfflinePlayer offlineTarget, String message) {

        String senderPrefix = PlayerDataStorage.instance.getPlayerPrefixByName(sender.getName());
        String senderSuffix = PlayerDataStorage.instance.getPlayerSuffixByName(sender.getName());
        String senderName = senderPrefix + sender.getName() + senderSuffix;

        String targetPrefix = PlayerDataStorage.instance.getPlayerPrefixByName(offlineTarget.getName());
        String targetSuffix = PlayerDataStorage.instance.getPlayerSuffixByName(offlineTarget.getName());
        String targetName = targetPrefix + offlineTarget.getName() + targetSuffix;

        Player target = Bukkit.getPlayer(offlineTarget.getName());

        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не найден!</red>"));
            return;
        }

        String formattedMessage = TagFormatter.format(message);

        String senderMessage = "<green>Вы</green> <yellow>→</yellow> "
                + targetName + ": <gray>" + formattedMessage + "</gray>";
        String targetMessage = "<click:suggest_command:'/msg " + sender.getName() + " '>"
                + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы ответить игроку "
                + senderName + ".'>" + senderName + "</hover></click> <yellow>→</yellow> <green>Вы</green>: <gray>" + formattedMessage + "</gray>";

        sender.sendMessage(MiniMessage.miniMessage().deserialize(senderMessage));
        target.sendMessage(MiniMessage.miniMessage().deserialize(targetMessage));

        String soundName = PlayerDataStorage.instance.getNotificationSound(target.getName());
        System.out.println(target.getName() + " " + soundName);
        Sound sound = Sounds.valueOf(soundName.toUpperCase()).sound;
        target.playSound(target.getLocation(), sound, 1.0f, 1.0f);

        MakeABuilders.instance.setLastMessaged(sender, target);
    }
}