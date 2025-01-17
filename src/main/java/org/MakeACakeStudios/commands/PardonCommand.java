package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
                        .required("player", OfflinePlayerParser.offlinePlayerParser()) // Парсер для OfflinePlayer
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player"))) // Хендлер команды
                        .build()
        );
    }

    private void handle(@NonNull CommandSender sender, @NotNull OfflinePlayer target) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String playerName = target.getName();

        String banStatus = PunishmentStorage.instance.checkBan(playerName);
        if (banStatus.contains("не забанен")) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Игрок " + playerName + " не забанен.</red>"));
            return;
        }

        String prefix = PlayerDataStorage.instance.getPlayerPrefixByName(playerName);
        String suffix = PlayerDataStorage.instance.getPlayerSuffixByName(playerName);
        String formattedName = prefix + playerName + suffix;

        PunishmentStorage.instance.pardonPlayer(playerName);
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);

        sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был разбанен.</green>"));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(miniMessage.deserialize("Игрок " + formattedName + " был разбанен."));
        }
    }
}
