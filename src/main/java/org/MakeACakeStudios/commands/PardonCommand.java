package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class PardonCommand implements Command {

    public PardonCommand() {
    }

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("pardon")
                        .permission("makeabuilders.pardon")
                        .required("player", OfflinePlayerParser.offlinePlayerParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player")))
                        .build()
        );
    }

    private void handle(@NonNull CommandSender sender, @NotNull OfflinePlayer target) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String playerName = target.getName();

        if (target.hasPlayedBefore() != true) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не найден!</red>"));
            return;
        }

        String banStatus = PunishmentStorage.instance.checkBan(playerName);
        if (banStatus.contains("не забанен")) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Игрок " + playerName + " не забанен.</red>"));
            return;
        }

        String formattedName = ChatUtils.getFormattedPlayerString(playerName, true);

        PunishmentStorage.instance.pardonPlayer(playerName);
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);

        sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был разбанен.</green>"));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(miniMessage.deserialize("Игрок " + formattedName + " был разбанен."));
        }
    }
}
