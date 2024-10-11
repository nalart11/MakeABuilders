package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.storage.*;

import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final MiniMessage miniMessage;
    private final PlayerNameStorage playerNameStorage;

    public UnmuteCommand(MakeABuilders plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerNameStorage = new PlayerNameStorage(plugin);
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

        String prefix = playerNameStorage.getPlayerPrefix(target);
        String suffix = playerNameStorage.getPlayerSuffix(target);
        String formattedName = prefix + target.getName() + suffix;

        // Проверяем, есть ли у игрока мут
        MuteCommand muteCommand = (MuteCommand) plugin.getCommand("mute").getExecutor();
        if (!muteCommand.isMuted(target)) {
            sender.sendMessage(miniMessage.deserialize("<red>✖ Игрок " + formattedName + " не замьючен.</red>"));
            return true;
        }

        // Убираем мут
        muteCommand.unmutePlayer(target);
        sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был размьючен.</green>"));
        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
        target.sendMessage(miniMessage.deserialize("<green>Вы были размьючены.</green>"));

        return true;
    }
}
