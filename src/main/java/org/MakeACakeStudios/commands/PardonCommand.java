package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;

public class PardonCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final MiniMessage miniMessage;
    private final PlayerDataStorage playerDataStorage;
    private final PunishmentStorage punishmentStorage;

    public PardonCommand(MakeABuilders plugin, PunishmentStorage punishmentStorage) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerDataStorage = new PlayerDataStorage(plugin);
        this.punishmentStorage = punishmentStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Используйте: /pardon <ник></red>"));
            return true;
        }

        String playerName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (target == null || target.getName() == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Игрок с именем " + playerName + " не найден.</red>"));
            return true;
        }

        String muteStatus = punishmentStorage.checkBan(playerName);
        if (muteStatus.contains("не забанен.")) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Игрок " + playerName + " не забанен.</red>"));
            return true;
        }

        String prefix = playerDataStorage.getPlayerPrefixByName(playerName);
        String suffix = playerDataStorage.getPlayerSuffixByName(playerName);
        String formattedName = prefix + target.getName() + suffix;

        punishmentStorage.pardonPlayer(playerName);
        sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был разбанен.</green>"));
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(miniMessage.deserialize("Игрок " + formattedName + " был разбанен."));
        }

        return true;
    }

}
