package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;

public class UnmuteCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final MiniMessage miniMessage;
    private final PlayerDataStorage playerDataStorage;
    private final PunishmentStorage punishmentStorage;

    public UnmuteCommand(MakeABuilders plugin, PunishmentStorage punishmentStorage) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerDataStorage = new PlayerDataStorage(plugin);
        this.punishmentStorage = punishmentStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Используйте: /unmute <ник></red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Игрок не найден.</red>"));
            return true;
        }

        String prefix = plugin.getPlayerPrefix(target);
        String suffix = plugin.getPlayerSuffix(target);
        String formattedName = prefix + target.getName() + suffix;

        String muteStatus = punishmentStorage.checkMute(target.getName());
        if (muteStatus.contains("не замьючен")) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Игрок " + formattedName + " не замьючен.</red>"));
            return true;
        }

        punishmentStorage.unmutePlayer(target.getName());
        sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был размьючен.</green>"));
        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
        target.sendMessage(miniMessage.deserialize("<green>Вы были размьючены.</green>"));

        return true;
    }
}
